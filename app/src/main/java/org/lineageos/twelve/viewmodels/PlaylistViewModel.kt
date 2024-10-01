/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.repositories.MediaRepository
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    mediaRepository: MediaRepository,
    futureMediaController: ListenableFuture<MediaController>
) : TwelveViewModel(mediaRepository, futureMediaController) {
    private val playlistUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist = playlistUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.playlist(it)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    fun loadPlaylist(playlistUri: Uri) {
        this.playlistUri.value = playlistUri
    }

    fun renamePlaylist(name: String) = viewModelScope.launch {
        playlistUri.value?.let { playlistUri ->
            mediaRepository.renamePlaylist(playlistUri, name)
        }
    }

    fun deletePlaylist() = viewModelScope.launch {
        playlistUri.value?.let { playlistUri ->
            mediaRepository.deletePlaylist(playlistUri)
        }
    }
}
