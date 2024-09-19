/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.guava.await
import org.lineageos.twelve.TwelveApplication
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.ext.playbackStatusFlow
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.services.PlaybackService
import kotlin.math.min

/**
 * Base view model for all app view models.
 * Here we keep the shared stuff every fragment could use, like access to the repository and
 * the media controller to interact with the playback service.
 */
abstract class TwelveViewModel(application: Application) : AndroidViewModel(application) {
    protected val mediaRepository = getApplication<TwelveApplication>().mediaRepository

    final override fun <T : Application> getApplication() = super.getApplication<T>()

    private val sessionToken by lazy {
        SessionToken(
            applicationContext,
            ComponentName(applicationContext, PlaybackService::class.java)
        )
    }

    private val mediaControllerFlow = channelFlow {
        val mediaController = MediaController.Builder(applicationContext, sessionToken)
            .buildAsync()
            .await()

        trySend(mediaController)

        awaitClose {
            mediaController.release()
        }
    }

    protected val mediaController = mediaControllerFlow
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playbackStatus = mediaController.filterNotNull()
        .flatMapLatest {
            // We need the player as well for the following logic
            it.playbackStatusFlow()
                .map { playbackStatus ->
                    Pair(it, playbackStatus)
                }
        }
        .flatMapLatest { (player, playbackStatus) ->
            // Poll the player for the updated progress
            val delayMs = playbackStatus.durationMs?.let { durationMs ->
                // Limit delay to the start of the next full second to ensure position display is
                // smooth.
                val mediaTimeUntilNextFullSecondMs = 1000 - durationMs % 1000
                val mediaTimeDelayMs = min(mediaTimeUntilNextFullSecondMs, MAX_UPDATE_INTERVAL_MS)

                // Calculate the delay until the next update in real time, taking playback speed into
                // account.
                // Constrain the delay to avoid too frequent / infrequent updates.
                val playbackSpeed = playbackStatus.playbackParameters.speed
                when (playbackSpeed > 0) {
                    true -> (mediaTimeDelayMs / playbackSpeed).toLong()
                    false -> MAX_UPDATE_INTERVAL_MS
                }.coerceIn(DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS, MAX_UPDATE_INTERVAL_MS)
            } ?: MAX_UPDATE_INTERVAL_MS

            flow {
                while (true) {
                    emit(
                        playbackStatus.copy(
                            currentPositionMs = playbackStatus.durationMs?.let {
                                player.currentPosition
                            }
                        )
                    )
                    delay(delayMs)
                }
            }
        }
        .mapLatest {
            RequestStatus.Success(it)
        }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RequestStatus.Loading()
        )

    fun playAudio(audio: List<Audio>, position: Int) {
        mediaController.value?.apply {
            setMediaItems(audio.map { it.toMediaItem() }, true)
            prepare()
            seekToDefaultPosition(position)
            play()
        }
    }

    private fun Audio.toMediaItem() = MediaItem.Builder()
        .setUri(uri)
        .setMimeType(mimeType)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistName)
                .setAlbumTitle(albumTitle)
                .build()
        )
        .build()

    companion object {
        private const val DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS = 200L
        private const val MAX_UPDATE_INTERVAL_MS = 1000L
    }
}
