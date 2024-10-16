package org.lineageos.twelve.repositories

import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.models.ResumptionPlaylist

/**
 * Manages the playlist used when the user wants to resume playback from the last queue.
 */
class ResumptionPlaylistRepository(val database: TwelveDatabase) {
    /**
     * Get the last resumption playlist or an empty one.
     */
    suspend fun getResumptionPlaylist() =
        database.getResumptionPlaylistDao().getResumptionPlaylistWithItems()?.let {
            val sortedItems = it.items.sortedBy { item -> item.playlistIndex }
            // This is needed to handle holes between indexes
            val startIndex = sortedItems.indexOfFirst { item ->
                item.playlistIndex == it.resumptionPlaylist.startIndex.toLong()
            }.takeIf { i -> i != -1 } ?: 0

            ResumptionPlaylist(
                sortedItems.map { item -> item.mediaId },
                startIndex,
                it.resumptionPlaylist.startPositionMs,
            )
        } ?: ResumptionPlaylist(emptyList())

    /**
     * When the user changes the queue, create a new resumption playlist.
     *
     * @param mediaIds The list of audio media item IDs
     * @param startIndex The start index
     * @param startPositionMs The playback position in milliseconds
     */
    suspend fun onMediaItemsChanged(
        mediaIds: List<String>,
        startIndex: Int,
        startPositionMs: Long,
    ) = database.getResumptionPlaylistDao().createResumptionPlaylist(
        startIndex,
        startPositionMs,
        mediaIds,
    )

    suspend fun onPlaybackPositionChanged(
        startIndex: Int,
        startPositionMs: Long,
    ) = database.getResumptionPlaylistDao().updateResumptionPlaylist(
        startIndex,
        startPositionMs,
    )
}
