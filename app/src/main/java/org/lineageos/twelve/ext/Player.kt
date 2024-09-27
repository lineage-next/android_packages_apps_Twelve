/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.channels.awaitClose
import org.lineageos.twelve.models.RepeatMode

@OptIn(UnstableApi::class)
fun Player.mediaMetadataFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            trySend(mediaMetadata)
        }
    }

    addListener(listener)
    trySend(mediaMetadata)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.mediaItemFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            trySend(mediaItem)
        }
    }

    addListener(listener)
    trySend(currentMediaItem)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.isPlayingFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            trySend(isPlaying)
        }
    }

    addListener(listener)
    trySend(isPlaying)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.shuffleModeFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            trySend(shuffleModeEnabled)
        }
    }

    addListener(listener)
    trySend(shuffleModeEnabled)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.repeatModeFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onRepeatModeChanged(repeatMode: Int) {
            trySend(typedRepeatMode)
        }
    }

    addListener(listener)
    trySend(typedRepeatMode)

    awaitClose {
        removeListener(listener)
    }
}

fun Player.playbackParametersFlow() = conflatedCallbackFlow {
    val listener = object : Player.Listener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            trySend(playbackParameters)
        }
    }

    addListener(listener)
    trySend(playbackParameters)

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
