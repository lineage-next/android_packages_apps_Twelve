/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

/**
 * Playback status reported by the service.
 */
data class PlaybackStatus(
    val mediaItem: MediaItem?,
    val mediaMetadata: MediaMetadata,
    val durationMs: Long?,
    val currentPositionMs: Long?,
    val isPlaying: Boolean,
    val shuffleModeEnabled: Boolean,
    val repeatMode: RepeatMode,
)
