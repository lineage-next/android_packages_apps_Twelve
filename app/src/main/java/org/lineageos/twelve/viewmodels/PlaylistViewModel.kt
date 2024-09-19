/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.net.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class PlaylistViewModel(application: Application) : TwelveViewModel(application) {
    private val playlistUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist = playlistUri.flatMapLatest {
        it?.let {
            mediaRepository.playlist(it)
        } ?: flowOf(null)
    }

    fun loadPlaylist(playlistUri: Uri) {
        this.playlistUri.value = playlistUri
    }
}
