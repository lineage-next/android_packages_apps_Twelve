/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri
import androidx.media3.common.MediaItem

/**
 * A media item.
 */
sealed interface MediaItem<T> : UniqueItem<T> {
    /**
     * A [Uri] identifying this media item.
     */
    val uri: Uri

    /**
     * Convert this item to a media item.
     */
    fun toMedia3MediaItem(): MediaItem
}
