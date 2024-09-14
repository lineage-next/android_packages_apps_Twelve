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
 * @param year The year of the album
 * @param thumbnail The album's thumbnail
 */
data class Album(
    val uri: Uri,
    val title: String,
    val artistUri: Uri,
    val year: Int?,
    val thumbnail: Bitmap?,
) : UniqueItem<Album> {
    override fun areItemsTheSame(other: Album) = this.uri == other.uri

    override fun areContentsTheSame(other: Album) = compareValuesBy(
        this, other,
        Album::title,
        Album::artistUri,
        Album::year,
        { it.thumbnail?.sameAs(other.thumbnail) ?: (other.thumbnail == null) },
    ) == 0
}
