/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.repositories

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.os.bundleOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.datasources.LocalDataSource
import org.lineageos.twelve.datasources.MediaDataSource
import org.lineageos.twelve.datasources.SubsonicDataSource
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.ProviderArgument.Companion.requireArgument
import org.lineageos.twelve.models.ProviderInstance
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.models.RequestStatus

/**
 * Media repository. This class coordinates all the data sources.
 * All methods that involves a URI as a parameter will be redirected to the
 * proper data source that can handle the media item. Methods that just returns a list of things
 * will be redirected to the provider selected by the user (see [navigationProviderInstance]).
 * If the navigation provider instance disappears, the local provider will be used.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaRepository(
    context: Context,
    scope: CoroutineScope,
    private val database: TwelveDatabase,
) {
    /**
     * Local data source singleton.
     */
    private val localDataSource = LocalDataSource(context, database)

    /**
     * Local provider singleton.
     */
    private val localProviderInstance = ProviderInstance(
        LOCAL_PROVIDER_INSTANCE_ID,
        Build.MODEL,
        ProviderType.LOCAL,
    )

    /**
     * All the providers.
     */
    private val allProviderInstancesToDataSource = combine(
        flowOf(listOf(localProviderInstance to localDataSource)),
        database.getSubsonicProviderDao().getAll().mapLatest { subsonicProviders ->
            subsonicProviders.map {
                val arguments = bundleOf(
                    SubsonicDataSource.ARG_SERVER.key to it.url,
                    SubsonicDataSource.ARG_USERNAME.key to it.username,
                    SubsonicDataSource.ARG_PASSWORD.key to it.password,
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION.key to
                            it.useLegacyAuthentication,
                )

                ProviderInstance(
                    it.id,
                    it.name,
                    ProviderType.SUBSONIC,
                ) to SubsonicDataSource(arguments)
            }
        }
    ) { providers -> providers.toList().flatten() }
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            listOf(localProviderInstance to localDataSource),
        )

    /**
     * All provider instances available to the app.
     */
    val allProviderInstances = allProviderInstancesToDataSource.mapLatest {
        it.map { (provider, _) -> provider }
    }

    /**
     * All the data sources available to the app.
     */
    private val allDataSources = allProviderInstancesToDataSource.mapLatest {
        it.map { (_, dataSource) -> dataSource }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            listOf(localDataSource),
        )

    /**
     * Given a provider type and a provider type ID, get the [ProviderInstance].
     */
    fun providerInstance(
        providerType: ProviderType, providerTypeId: Long
    ) = allProviderInstances.mapLatest {
        it.firstOrNull { providerInstance ->
            providerType == providerInstance.type && providerTypeId == providerInstance.typeId
        }
    }

    /**
     * Given a provider type and a provider type ID, get the [Bundle] containing the arguments
     */
    fun providerArguments(
        providerType: ProviderType, providerTypeId: Long
    ) = when (providerType) {
        ProviderType.LOCAL -> flowOf(bundleOf())

        ProviderType.SUBSONIC -> database.getSubsonicProviderDao().getById(
            providerTypeId
        ).mapLatest { subsonicProvider ->
            subsonicProvider?.let {
                bundleOf(
                    SubsonicDataSource.ARG_SERVER.key to it.url,
                    SubsonicDataSource.ARG_USERNAME.key to it.username,
                    SubsonicDataSource.ARG_PASSWORD.key to it.password,
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION.key to
                            it.useLegacyAuthentication,
                )
            }
        }
    }

    /**
     * Given a provider instance, get the corresponding [MediaDataSource].
     */
    private fun dataSource(
        providerType: ProviderType, providerTypeId: Long
    ) = allProviderInstancesToDataSource.mapLatest {
        it.firstOrNull { (provider, _) ->
            providerType == provider.type && providerTypeId == provider.typeId
        }?.second
    }

    /**
     * Given a provider instance, get the corresponding [MediaDataSource].
     */
    private fun dataSource(provider: ProviderInstance) = dataSource(
        provider.type, provider.typeId
    )

    private var _navigationProviderInstance = MutableStateFlow(
        ProviderType.LOCAL to LOCAL_PROVIDER_INSTANCE_ID
    )

    /**
     * The current navigation provider. This is used when the user looks for all media types,
     * like the home page, or with the search feature.
     */
    val navigationProviderInstance = _navigationProviderInstance
        .flatMapLatest {
            providerInstance(it.first, it.second).mapLatest { currentNavigationProvider ->
                // Default to local provider if not found
                currentNavigationProvider ?: localProviderInstance
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            localProviderInstance,
        )

    /**
     * Change the default navigation provider.
     */
    fun setNavigationProviderInstance(providerInstance: ProviderInstance) {
        _navigationProviderInstance.value = providerInstance.type to providerInstance.typeId
    }

    private val navigationDataSource = navigationProviderInstance
        .flatMapLatest {
            dataSource(it).mapLatest { dataSource ->
                dataSource ?: localDataSource
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            localDataSource,
        )

    /**
     * Add a new provider to the database.
     */
    suspend fun addProvider(providerType: ProviderType, name: String, arguments: Bundle) {
        when (providerType) {
            ProviderType.LOCAL -> throw Exception("Cannot create local providers")

            ProviderType.SUBSONIC -> {
                val server = arguments.requireArgument(SubsonicDataSource.ARG_SERVER)
                val username = arguments.requireArgument(SubsonicDataSource.ARG_USERNAME)
                val password = arguments.requireArgument(SubsonicDataSource.ARG_PASSWORD)
                val useLegacyAuthentication = arguments.requireArgument(
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION
                )

                database.getSubsonicProviderDao().create(
                    name, server, username, password, useLegacyAuthentication
                )
            }
        }
    }

    /**
     * Update an already existing provider.
     *
     * @param providerType The [ProviderType]
     * @param providerTypeId The [ProviderType] specific provider ID
     * @param name The updated name
     * @param arguments The updated arguments
     */
    suspend fun updateProvider(
        providerType: ProviderType,
        providerTypeId: Long,
        name: String,
        arguments: Bundle
    ) {
        when (providerType) {
            ProviderType.LOCAL -> throw Exception("Cannot update local providers")

            ProviderType.SUBSONIC -> {
                val server = arguments.requireArgument(SubsonicDataSource.ARG_SERVER)
                val username = arguments.requireArgument(SubsonicDataSource.ARG_USERNAME)
                val password = arguments.requireArgument(SubsonicDataSource.ARG_PASSWORD)
                val useLegacyAuthentication = arguments.requireArgument(
                    SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION
                )

                database.getSubsonicProviderDao().update(
                    providerTypeId,
                    name,
                    server,
                    username,
                    password,
                    useLegacyAuthentication,
                )
            }
        }
    }

    suspend fun deleteProvider(providerType: ProviderType, providerTypeId: Long) {
        when (providerType) {
            ProviderType.LOCAL -> throw Exception("Cannot delete local providers")

            ProviderType.SUBSONIC -> database.getSubsonicProviderDao().delete(providerTypeId)
        }
    }

    private fun <T> dataSourceOfMediaItems(
        vararg uris: Uri, predicate: MediaDataSource.() -> Flow<RequestStatus<T>>
    ) = allDataSources.flatMapLatest {
        it.firstOrNull { dataSource ->
            uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
        }?.predicate() ?: flowOf(RequestStatus.Error(RequestStatus.Error.Type.NOT_FOUND))
    }

    private suspend fun <T> getDataSourceOfMediaItems(
        vararg uris: Uri, predicate: suspend MediaDataSource.() -> RequestStatus<T>
    ) = allDataSources.value.firstOrNull { dataSource ->
        uris.all { uri -> dataSource.isMediaItemCompatible(uri) }
    }?.predicate() ?: RequestStatus.Error(RequestStatus.Error.Type.NOT_FOUND)

    /**
     * @see MediaDataSource.albums
     */
    fun albums(): Flow<RequestStatus<List<Album>>> =
        navigationDataSource.flatMapLatest { it.albums() }

    /**
     * @see MediaDataSource.artists
     */
    fun artists(): Flow<RequestStatus<List<Artist>>> =
        navigationDataSource.flatMapLatest { it.artists() }

    /**
     * @see MediaDataSource.genres
     */
    fun genres(): Flow<RequestStatus<List<Genre>>> =
        navigationDataSource.flatMapLatest { it.genres() }

    /**
     * @see MediaDataSource.playlists
     */
    fun playlists(): Flow<RequestStatus<List<Playlist>>> =
        navigationDataSource.flatMapLatest { it.playlists() }

    /**
     * @see MediaDataSource.search
     */
    fun search(query: String): Flow<RequestStatus<List<MediaItem<*>>>> =
        navigationDataSource.flatMapLatest { it.search(query) }

    /**
     * @see MediaDataSource.audio
     */
    fun audio(audioUri: Uri): Flow<RequestStatus<Audio>> = dataSourceOfMediaItems(audioUri) {
        audio(audioUri)
    }

    /**
     * @see MediaDataSource.album
     */
    fun album(albumUri: Uri): Flow<RequestStatus<Pair<Album, List<Audio>>>> =
        dataSourceOfMediaItems(albumUri) {
            album(albumUri)
        }

    /**
     * @see MediaDataSource.artist
     */
    fun artist(artistUri: Uri): Flow<RequestStatus<Pair<Artist, ArtistWorks>>> =
        dataSourceOfMediaItems(artistUri) {
            artist(artistUri)
        }

    /**
     * @see MediaDataSource.playlist
     */
    fun playlist(playlistUri: Uri): Flow<RequestStatus<Pair<Playlist, List<Audio?>>>> =
        dataSourceOfMediaItems(playlistUri) {
            playlist(playlistUri)
        }

    /**
     * @see MediaDataSource.audioPlaylistsStatus
     */
    fun audioPlaylistsStatus(audioUri: Uri): Flow<RequestStatus<List<Pair<Playlist, Boolean>>>> =
        dataSourceOfMediaItems(audioUri) {
            audioPlaylistsStatus(audioUri)
        }

    /**
     * @see MediaDataSource.createPlaylist
     */
    suspend fun createPlaylist(name: String): RequestStatus<Uri> =
        navigationDataSource.value.createPlaylist(name)

    /**
     * @see MediaDataSource.renamePlaylist
     */
    suspend fun renamePlaylist(playlistUri: Uri, name: String): RequestStatus<Unit> =
        getDataSourceOfMediaItems(playlistUri) {
            renamePlaylist(playlistUri, name)
        }

    /**
     * @see MediaDataSource.deletePlaylist
     */
    suspend fun deletePlaylist(playlistUri: Uri): RequestStatus<Unit> =
        getDataSourceOfMediaItems(playlistUri) {
            deletePlaylist(playlistUri)
        }

    /**
     * @see MediaDataSource.addAudioToPlaylist
     */
    suspend fun addAudioToPlaylist(playlistUri: Uri, audioUri: Uri): RequestStatus<Unit> =
        getDataSourceOfMediaItems(playlistUri, audioUri) {
            addAudioToPlaylist(playlistUri, audioUri)
        }

    /**
     * @see MediaDataSource.removeAudioFromPlaylist
     */
    suspend fun removeAudioFromPlaylist(playlistUri: Uri, audioUri: Uri): RequestStatus<Unit> =
        getDataSourceOfMediaItems(playlistUri, audioUri) {
            removeAudioFromPlaylist(playlistUri, audioUri)
        }

    companion object {
        private const val LOCAL_PROVIDER_INSTANCE_ID = 0L
    }
}
