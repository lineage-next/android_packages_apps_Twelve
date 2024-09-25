/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.lineageos.twelve.models.PlaybackStatus
import org.lineageos.twelve.models.RepeatMode

@OptIn(UnstableApi::class)
fun Player.playbackStatusFlow() = callbackFlow {
    val updatePlaybackStatus = {
        trySend(
            PlaybackStatus(
                currentMediaItem,
                mediaMetadata,
                isPlaying,
                shuffleModeEnabled,
                typedRepeatMode,
                playbackParameters.speed,
            )
        )
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
