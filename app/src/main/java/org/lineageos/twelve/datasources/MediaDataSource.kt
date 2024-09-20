/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.UniqueItem

/**
 * A data source for media.
 */
interface MediaDataSource {
    /**
     * Get all the albums. All albums must have at least one audio associated with them.
     */
    fun albums(): Flow<RequestStatus<List<Album>>>

    /**
     * Get all the artists. All artists must have at least one audio associated with them.
     */
    fun artists(): Flow<RequestStatus<List<Artist>>>

    /**
     * Get all the genres. All genres must have at least one audio associated with them.
     */
    fun genres(): Flow<RequestStatus<List<Genre>>>

    /**
     * Get all the playlists. A playlist can be empty.
     */
    fun playlists(): Flow<RequestStatus<List<Playlist>>>

    /**
     * Start a search for the given query.
     * Only the following items can be returned: [Album], [Artist], [Audio], [Genre], [Playlist].
     */
    fun search(query: String): Flow<RequestStatus<List<UniqueItem>>>

    /**
     * Get the album information and all the tracks of the given album.
     */
    fun album(albumUri: Uri): Flow<RequestStatus<Pair<Album, List<Audio>>>>

    /**
     * Get the artist information and all the works associated with them.
     */
    fun artist(artistUri: Uri): Flow<RequestStatus<Pair<Artist, ArtistWorks>>>

    /**
     * Get the genre information and all the tracks of the given genre.
     */
    fun genre(genreUri: Uri): Flow<RequestStatus<Pair<Genre, List<Audio>>>>

    /**
     * Get the playlist information and all the tracks of the given playlist.
     */
    fun playlist(playlistUri: Uri): Flow<RequestStatus<Pair<Playlist, List<Audio>>>>
}
