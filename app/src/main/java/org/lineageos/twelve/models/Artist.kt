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
) : UniqueItem {
    override fun areItemsTheSame(other: UniqueItem) =
        UniqueItem.areContentsTheSame(Artist::class, other) {
            this.uri == it.uri
        }

    override fun areContentsTheSame(other: UniqueItem) =
        UniqueItem.areContentsTheSame(Artist::class, other) {
            compareValuesBy(
                this, it,
                Artist::name,
                { it.thumbnail?.sameAs(it.thumbnail) ?: (it.thumbnail == null) },
            ) == 0
        }
}
