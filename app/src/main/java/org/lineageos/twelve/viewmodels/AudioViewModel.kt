/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.RequestStatus

open class AudioViewModel(application: Application) : TwelveViewModel(application) {
    protected val audioUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val audio = audioUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.audio(it)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    fun loadAudio(audioUri: Uri) {
        this.audioUri.value = audioUri
    }

    fun addToQueue(audio: Audio) = viewModelScope.launch {
        mediaController.value?.addMediaItem(audio.toMedia3MediaItem())
    }

    fun playNext(audio: Audio) = viewModelScope.launch {
        mediaController.value?.let { controller ->
            controller.addMediaItem(
                controller.currentMediaItemIndex + 1,
                audio.toMedia3MediaItem()
            )
        }
    }

    fun addToPlaylist(playlistUri: Uri) = viewModelScope.launch {
        audioUri.value?.let {
            mediaRepository.addAudioToPlaylist(playlistUri, it)
        }
    }

    fun removeFromPlaylist(playlistUri: Uri) = viewModelScope.launch {
        audioUri.value?.let {
            mediaRepository.removeAudioFromPlaylist(playlistUri, it)
        }
    }
}
