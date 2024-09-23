/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import androidx.media3.common.MediaMetadata
import org.lineageos.twelve.models.Audio.Type

fun Type.toMediaMetadataType(): @MediaMetadata.MediaType Int {
    return when (this) {
        Type.MUSIC -> MediaMetadata.MEDIA_TYPE_MUSIC
        Type.PODCAST -> MediaMetadata.MEDIA_TYPE_PODCAST
        Type.AUDIOBOOK -> MediaMetadata.MEDIA_TYPE_AUDIO_BOOK
        else -> MediaMetadata.MEDIA_TYPE_MUSIC
    }
}
