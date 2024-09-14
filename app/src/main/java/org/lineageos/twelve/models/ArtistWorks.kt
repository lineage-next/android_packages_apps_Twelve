/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Whatever an artist has worked on.
 *
 * @param albums Albums on which the artist appears
 * @param playlists Playlists on which the artist appears
 */
data class ArtistWorks(
    val albums: List<Album>,
    val playlists: List<Playlist>,
)
