/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.graphics.Bitmap
import android.net.Uri
import androidx.media3.common.MediaMetadata
import org.lineageos.twelve.ext.buildMediaItem

/**
 * An artist.
 *
 * @param uri The URI of the artist
 * @param name The name of the artist
 * @param thumbnail The artist's thumbnail
 */
data class Artist(
    val uri: Uri,
    val name: String,
    val thumbnail: Bitmap?,
) : UniqueItem<Artist> {
    override fun areItemsTheSame(other: Artist) = this.uri == other.uri

    override fun areContentsTheSame(other: Artist) = compareValuesBy(
        this, other,
        Artist::name,
        { it.thumbnail?.sameAs(other.thumbnail) ?: (other.thumbnail == null) },
    ) == 0

    override fun toMediaItem() = buildMediaItem(
        title = name,
        mediaId = "$ARTIST_MEDIA_ITEM_ID_PREFIX${uri}",
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_ARTIST,
        sourceUri = uri,
    )

    companion object {
        const val ARTIST_MEDIA_ITEM_ID_PREFIX = "[artist]"
    }
}
