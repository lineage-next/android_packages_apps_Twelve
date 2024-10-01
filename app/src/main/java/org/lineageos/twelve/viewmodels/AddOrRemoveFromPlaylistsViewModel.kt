/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.repositories.MediaRepository
import javax.inject.Inject

@HiltViewModel
class AddOrRemoveFromPlaylistsViewModel @Inject constructor(
    mediaRepository: MediaRepository,
    futureMediaController: ListenableFuture<MediaController>
) : AudioViewModel(mediaRepository, futureMediaController) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val playlistToHasAudio = audioUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.audioPlaylistsStatus(it)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )
}
