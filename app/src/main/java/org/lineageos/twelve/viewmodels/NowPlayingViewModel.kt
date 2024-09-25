/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import org.lineageos.twelve.ext.next
import org.lineageos.twelve.ext.typedRepeatMode

class NowPlayingViewModel(application: Application) : TwelveViewModel(application) {
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
