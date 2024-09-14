/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import androidx.media3.common.C
import androidx.media3.common.Player
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.lineageos.twelve.models.PlaybackStatus
import org.lineageos.twelve.models.RepeatMode

fun Player.playbackStatusFlow() = callbackFlow {
    val updatePlaybackStatus = {
        val duration = duration.takeIf { it != C.TIME_UNSET }

        val playbackStatus = PlaybackStatus(
            currentMediaItem,
            mediaMetadata,
            duration,
            currentPosition.takeIf { duration != null },
            isPlaying,
            shuffleModeEnabled,
            typedRepeatMode,
        )

        trySend(playbackStatus)
    }

    val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)

            updatePlaybackStatus()
        }
    }

    addListener(listener)
    updatePlaybackStatus()

    awaitClose {
        removeListener(listener)
    }
}

var Player.typedRepeatMode: RepeatMode
    get() = when (repeatMode) {
        Player.REPEAT_MODE_OFF -> RepeatMode.NONE
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> throw Exception("Unknown repeat mode")
    }
    set(value) {
        repeatMode = when (value) {
            RepeatMode.NONE -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }
