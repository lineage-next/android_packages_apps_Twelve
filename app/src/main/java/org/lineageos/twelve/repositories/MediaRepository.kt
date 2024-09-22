/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.repositories

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.datasources.LocalDataSource
import org.lineageos.twelve.datasources.MediaDataSource
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.UniqueItem

class MediaRepository(context: Context, database: TwelveDatabase) {
    private val localDataSource = LocalDataSource(context, database)

    /**
     * @see MediaDataSource.albums
     */
    fun albums(): Flow<RequestStatus<List<Album>>> = localDataSource.albums()

    /**
     * @see MediaDataSource.artists
     */
    fun artists(): Flow<RequestStatus<List<Artist>>> = localDataSource.artists()

    /**
     * @see MediaDataSource.genres
     */
    fun genres(): Flow<RequestStatus<List<Genre>>> = localDataSource.genres()

    /**
     * @see MediaDataSource.playlists
     */
    fun playlists(): Flow<RequestStatus<List<Playlist>>> = localDataSource.playlists()

    /**
     * @see MediaDataSource.search
     */
    fun search(query: String): Flow<RequestStatus<List<UniqueItem<*>>>> =
        localDataSource.search(query)

    /**
     * @see MediaDataSource.audio
     */
    fun audio(audioUri: Uri): Flow<RequestStatus<Audio>> = localDataSource.audio(audioUri)

    /**
     * @see MediaDataSource.album
     */
    fun album(albumUri: Uri): Flow<RequestStatus<Pair<Album, List<Audio>>>> =
        localDataSource.album(albumUri)

    /**
     * @see MediaDataSource.artist
     */
    fun artist(artistUri: Uri): Flow<RequestStatus<Pair<Artist, ArtistWorks>>> =
        localDataSource.artist(artistUri)

    /**
     * @see MediaDataSource.playlist
     */
    fun playlist(playlistUri: Uri): Flow<RequestStatus<Pair<Playlist, List<Audio?>>>> =
        localDataSource.playlist(playlistUri)

    /**
     * @see MediaDataSource.createPlaylist
     */
    suspend fun createPlaylist(name: String): RequestStatus<Uri> =
        localDataSource.createPlaylist(name)

    /**
     * @see MediaDataSource.renamePlaylist
     */
    suspend fun renamePlaylist(playlistUri: Uri, name: String): RequestStatus<Unit> =
        localDataSource.renamePlaylist(playlistUri, name)

    /**
     * @see MediaDataSource.deletePlaylist
     */
    suspend fun deletePlaylist(playlistUri: Uri): RequestStatus<Unit> =
        localDataSource.deletePlaylist(playlistUri)

    /**
     * @see MediaDataSource.addAudioToPlaylist
     */
    suspend fun addAudioToPlaylist(playlistUri: Uri, audioUri: Uri): RequestStatus<Unit> =
        localDataSource.addAudioToPlaylist(playlistUri, audioUri)

    /**
     * @see MediaDataSource.removeAudioFromPlaylist
     */
    suspend fun removeAudioFromPlaylist(playlistUri: Uri, audioUri: Uri): RequestStatus<Unit> =
        localDataSource.removeAudioFromPlaylist(playlistUri, audioUri)

    /**
     * @see MediaDataSource.audioPlaylistsStatus
     */
    fun audioPlaylistsStatus(audioUri: Uri): Flow<RequestStatus<List<Pair<Playlist, Boolean>>>> =
        localDataSource.audioPlaylistsStatus(audioUri)
}
