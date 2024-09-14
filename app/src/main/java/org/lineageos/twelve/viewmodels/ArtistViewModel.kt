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

class ArtistViewModel(application: Application) : TwelveViewModel(application) {
    private val artistUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val artist = artistUri.flatMapLatest {
        it?.let {
            mediaRepository.artist(it)
        } ?: flowOf(null)
    }

    fun loadAlbum(artistUri: Uri) {
        this.artistUri.value = artistUri
    }
}
