/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import org.lineageos.twelve.ext.next
import org.lineageos.twelve.ext.typedRepeatMode

class NowPlayingViewModel(application: Application) : TwelveViewModel(application) {
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
}
