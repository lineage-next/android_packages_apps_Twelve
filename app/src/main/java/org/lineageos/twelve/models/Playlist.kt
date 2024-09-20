/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri

/**
 * A user-defined playlist.
 *
 * @param uri The URI of the playlist
 * @param name The name of the playlist
 */
data class Playlist(
    val uri: Uri,
    val name: String,
) : UniqueItem {
    override fun areItemsTheSame(other: UniqueItem) =
        UniqueItem.areItemsTheSame(Playlist::class, other) {
            uri == it.uri
        }

    override fun areContentsTheSame(other: UniqueItem) =
        UniqueItem.areContentsTheSame(Playlist::class, other) {
            compareValuesBy(
                this, it,
                Playlist::name,
            ) == 0
        }
}
