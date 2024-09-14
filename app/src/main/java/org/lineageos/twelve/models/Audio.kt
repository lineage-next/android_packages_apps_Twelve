/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri

/**
 * An audio.
 *
 * @param uri The URI of the audio
 * @param mimeType The MIME type of the audio
 * @param title The title of the audio
 * @param type The type of the audio
 * @param durationMs The duration of the audio in milliseconds
 * @param artistUri The URI of the artist of the audio
 * @param albumUri The URI of the album of the audio
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
    val albumUri: Uri,
    val albumTrack: Int,
    val genreUri: Uri,
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
        Audio::albumUri,
        Audio::albumTrack,
        Audio::genreUri,
        Audio::year,
    ) == 0
}
