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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class AudioViewModel(application: Application) : TwelveViewModel(application) {
    protected val audioUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val audio = audioUri.flatMapLatest {
        it?.let {
            mediaRepository.audio(it)
        } ?: flowOf(null)
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null
        )

    fun loadAudio(audioUri: Uri) {
        this.audioUri.value = audioUri
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
