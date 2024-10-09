/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.ext.availableCommandsFlow
import org.lineageos.twelve.ext.isPlayingFlow
import org.lineageos.twelve.ext.mediaItemFlow
import org.lineageos.twelve.ext.mediaMetadataFlow
import org.lineageos.twelve.ext.next
import org.lineageos.twelve.ext.playbackParametersFlow
import org.lineageos.twelve.ext.repeatModeFlow
import org.lineageos.twelve.ext.shuffleModeFlow
import org.lineageos.twelve.ext.tracksFlow
import org.lineageos.twelve.ext.typedRepeatMode
import org.lineageos.twelve.models.RepeatMode

open class NowPlayingViewModel(application: Application) : TwelveViewModel(application) {
    enum class PlaybackSpeed(val value: Float) {
        ONE(1f),
        ONE_POINT_FIVE(1.5f),
        TWO(2f),
        ZERO_POINT_FIVE(0.5f);

        companion object {
            fun fromValue(value: Float) = entries.firstOrNull {
                it.value == value
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaMetadata = mediaController
        .filterNotNull()
        .flatMapLatest { it.mediaMetadataFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = MediaMetadata.EMPTY
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaItem = mediaController
        .filterNotNull()
        .flatMapLatest { it.mediaItemFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val isPlaying = mediaController
        .filterNotNull()
        .flatMapLatest { it.isPlayingFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val shuffleMode = mediaController
        .filterNotNull()
        .flatMapLatest { it.shuffleModeFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val repeatMode = mediaController
        .filterNotNull()
        .flatMapLatest { it.repeatModeFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RepeatMode.NONE
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playbackParameters = mediaController
        .filterNotNull()
        .flatMapLatest { it.playbackParametersFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentTrackFormat = mediaController
        .filterNotNull()
        .flatMapLatest { it.tracksFlow() }
        .flowOn(Dispatchers.Main)
        .mapLatest { tracks ->
            val groups = tracks.groups.filter { group ->
                group.type == C.TRACK_TYPE_AUDIO && group.isSelected
            }

            require(groups.size <= 1) { "More than one audio track selected" }

            groups.firstOrNull()?.let { group ->
                (0..group.length).firstNotNullOfOrNull { i ->
                    when (group.isTrackSelected(i)) {
                        true -> group.getTrackFormat(i)
                        false -> null
                    }
                }
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val mimeType = combine(currentTrackFormat, mediaItem) { format, mediaItem ->
        format?.sampleMimeType
            ?: format?.containerMimeType
            ?: mediaItem?.localConfiguration?.mimeType
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(ExperimentalCoroutinesApi::class)
    val displayFileType = mimeType
        .mapLatest { mimeType ->
            mimeType?.let {
                MimeTypes.normalizeMimeType(it)
            }?.let {
                it.takeIf { it.contains('/') }
                    ?.substringAfterLast('/')
                    ?.uppercase()
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableCommands = mediaController
        .filterNotNull()
        .flatMapLatest { it.availableCommandsFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val durationCurrentPositionMs = mediaController
        .filterNotNull()
        .flatMapLatest { mediaController ->
            flow {
                while (true) {
                    val duration = mediaController.duration.takeIf { it != C.TIME_UNSET }
                    emit(
                        Triple(
                            duration,
                            duration?.let { mediaController.currentPosition },
                            mediaController.playbackParameters.speed,
                        )
                    )
                    delay(200)
                }
            }
        }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Triple(null, null, 1f)
        )

    fun togglePlayPause() {
        mediaController.value?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekToPosition(positionMs: Long) {
        mediaController.value?.seekTo(positionMs)
    }

    fun seekToPrevious() {
        mediaController.value?.let {
            val currentMediaItemIndex = it.currentMediaItemIndex
            it.seekToPrevious()
            if (it.currentMediaItemIndex < currentMediaItemIndex) {
                it.play()
            }
        }
    }

    fun seekToNext() {
        mediaController.value?.let {
            it.seekToNext()
            it.play()
        }
    }

    fun toggleShuffleMode() {
        mediaController.value?.apply {
            shuffleModeEnabled = shuffleModeEnabled.not()
        }
    }

    fun toggleRepeatMode() {
        mediaController.value?.apply {
            typedRepeatMode = typedRepeatMode.next()
        }
    }

    fun shufflePlaybackSpeed() {
        mediaController.value?.let {
            val playbackSpeed = PlaybackSpeed.fromValue(
                it.playbackParameters.speed
            ) ?: PlaybackSpeed.ONE

            it.setPlaybackSpeed(playbackSpeed.next().value)
        }
    }
}
