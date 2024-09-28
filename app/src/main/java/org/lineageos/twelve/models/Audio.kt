/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri
import androidx.media3.common.MediaMetadata
import org.lineageos.twelve.ext.buildMediaItem

/**
 * An audio.
 *
 * @param uri The URI of the audio
 * @param mimeType The MIME type of the audio
 * @param title The title of the audio
 * @param type The type of the audio
 * @param durationMs The duration of the audio in milliseconds
 * @param artistUri The URI of the artist of the audio
 * @param artistName The name of the artist of the audio
 * @param albumUri The URI of the album of the audio
 * @param albumTitle The title of the album of the audio
 * @param albumTrack The track number of the audio in the album
 * @param genreUri The URI of the genre of the audio
 * @param year The year of release of the audio
 */
data class Audio(
    override val uri: Uri,
    val mimeType: String,
    val title: String,
    val type: Type,
    val durationMs: Int,
    val artistUri: Uri,
    val artistName: String,
    val albumUri: Uri,
    val albumTitle: String,
    val albumTrack: Int,
    val genreUri: Uri,
    val genre: String?,
    val year: Int,
) : MediaItem<Audio> {
    enum class Type(
        val media3MediaType: @MediaMetadata.MediaType Int,
    ) {
        /**
         * Music.
         */
        MUSIC(MediaMetadata.MEDIA_TYPE_MUSIC),

        /**
         * Podcast.
         */
        PODCAST(MediaMetadata.MEDIA_TYPE_PODCAST),

        /**
         * Audiobook.
         */
        AUDIOBOOK(MediaMetadata.MEDIA_TYPE_AUDIO_BOOK),

        /**
         * Recording.
         */
        RECORDING(MediaMetadata.MEDIA_TYPE_MUSIC),
    }

    override fun areItemsTheSame(other: Audio) = this.uri == other.uri

    override fun areContentsTheSame(other: Audio) = compareValuesBy(
        this, other,
        Audio::mimeType,
        Audio::title,
        Audio::type,
        Audio::durationMs,
        Audio::artistUri,
        Audio::artistName,
        Audio::albumUri,
        Audio::albumTitle,
        Audio::albumTrack,
        Audio::genreUri,
        Audio::genre,
        Audio::year,
    ) == 0

    override fun toMedia3MediaItem() = buildMediaItem(
        title = title,
        mediaId = "$AUDIO_MEDIA_ITEM_ID_PREFIX${uri}",
        isPlayable = true,
        isBrowsable = false,
        mediaType = type.media3MediaType,
        album = albumTitle,
        artist = artistName,
        genre = genre,
        sourceUri = uri,
        mimeType = mimeType,
    )

    companion object {
        const val AUDIO_MEDIA_ITEM_ID_PREFIX = "[audio]"
    }
}
