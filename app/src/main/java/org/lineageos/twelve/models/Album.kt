/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.graphics.Bitmap
import android.net.Uri

/**
 * An album.
 *
 * @param uri The URI of the album
 * @param title The title of the album
 * @param artistUri The URI of the artist
 * @param artistName The name of the artist
 * @param year The year of the album
 * @param thumbnail The album's thumbnail
 */
data class Album(
    val uri: Uri,
    val title: String,
    val artistUri: Uri,
    val artistName: String,
    val year: Int?,
    val thumbnail: Bitmap?,
) : UniqueItem {
    override fun areItemsTheSame(other: UniqueItem) =
        UniqueItem.areItemsTheSame(Album::class, other)
        {
            this.uri == it.uri
        }

    override fun areContentsTheSame(other: UniqueItem) =
        UniqueItem.areContentsTheSame(Album::class, other) {
            compareValuesBy(
                this, it,
                Album::title,
                Album::artistUri,
                Album::artistName,
                Album::year,
                { it.thumbnail?.sameAs(it.thumbnail) ?: (it.thumbnail == null) },
            ) == 0
        }
}
