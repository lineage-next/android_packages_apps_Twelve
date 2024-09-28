/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri
import org.lineageos.twelve.ext.buildMediaItem
import org.lineageos.twelve.ext.toMediaMetadataType

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
    val uri: Uri,
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
) : UniqueItem<Audio> {
    enum class Type {
        /**
         * Music.
         */
        MUSIC,

        /**
         * Podcast.
         */
        PODCAST,

        /**
         * Audiobook.
         */
        AUDIOBOOK,

        /**
         * Recording.
         */
        RECORDING,
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

    override fun toMediaItem() = buildMediaItem(
        title = title,
        mediaId = "$AUDIO_MEDIA_ITEM_ID_PREFIX${uri}",
        isPlayable = true,
        isBrowsable = false,
        mediaType = type.toMediaMetadataType(),
        album = albumTitle,
        artist = artistName,
        genre = genre,
        sourceUri = uri,
    )

    companion object {
        const val AUDIO_MEDIA_ITEM_ID_PREFIX = "[audio]"
    }
}
