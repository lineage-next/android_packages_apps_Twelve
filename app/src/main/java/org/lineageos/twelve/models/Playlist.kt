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
) : UniqueItem<Playlist> {
    override fun areItemsTheSame(other: Playlist) = uri == other.uri

    override fun areContentsTheSame(other: Playlist) = compareValuesBy(
        this,
        other,
        Playlist::name,
    ) == 0
}
