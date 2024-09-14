/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.graphics.Bitmap
import android.net.Uri

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
}
