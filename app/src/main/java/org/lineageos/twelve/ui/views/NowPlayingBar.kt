/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.slideDown
import org.lineageos.twelve.ext.slideUp
import org.lineageos.twelve.models.PlaybackStatus

class NowPlayingBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val artistNameTextView by lazy { findViewById<TextView>(R.id.artistNameTextView) }
    private val albumTitleTextView by lazy { findViewById<TextView>(R.id.albumTitleTextView) }
    private val circularProgressIndicator by lazy { findViewById<CircularProgressIndicator>(R.id.circularProgressIndicator) }
    private val materialCardView by lazy { findViewById<MaterialCardView>(R.id.materialCardView) }
    private val playPauseImageButton by lazy { findViewById<ImageButton>(R.id.playPauseImageButton) }
    private val thumbnailImageView by lazy { findViewById<ImageView>(R.id.thumbnailImageView) }
    private val titleTextView by lazy { findViewById<TextView>(R.id.titleTextView) }

    init {
        inflate(context, R.layout.now_playing_bar, this)

        circularProgressIndicator.min = 0
    }

    fun setOnPlayPauseClickListener(l: OnClickListener?) =
        playPauseImageButton.setOnClickListener(l)

    fun setOnNowPlayingClickListener(l: OnClickListener?) {
        materialCardView.setOnClickListener(l)
    }

    fun updatePlaybackStatus(playbackStatus: PlaybackStatus) {
        playbackStatus.mediaMetadata.artworkData?.also { artworkData ->
            BitmapFactory.decodeByteArray(
                artworkData, 0, artworkData.size
            )?.let { bitmap ->
                thumbnailImageView.setImageBitmap(bitmap)
            }
        } ?: playbackStatus.mediaMetadata.artworkUri?.also { artworkUri ->
            thumbnailImageView.setImageURI(artworkUri)
        } ?: thumbnailImageView.setImageResource(R.drawable.ic_music_note)

        playbackStatus.mediaMetadata.title?.also {
            titleTextView.text = it
            titleTextView.isVisible = true
        } ?: run {
            titleTextView.isVisible = false
        }

        playbackStatus.mediaMetadata.artist?.also {
            artistNameTextView.text = it
            artistNameTextView.isVisible = true
        } ?: run {
            artistNameTextView.isVisible = false
        }

        playbackStatus.mediaMetadata.albumTitle?.also {
            albumTitleTextView.text = it
            albumTitleTextView.isVisible = true
        } ?: run {
            albumTitleTextView.isVisible = false
        }

        playPauseImageButton.setImageResource(
            when (playbackStatus.isPlaying) {
                true -> R.drawable.ic_pause
                false -> R.drawable.ic_play_arrow
            }
        )

        val currentPositionSecs = playbackStatus.currentPositionMs?.let { currentPositionMs ->
            currentPositionMs / 1000
        }?.toInt() ?: 0
        val durationSecs = playbackStatus.durationMs?.let { durationMs ->
            durationMs / 1000
        }?.toInt()?.takeIf { it != 0 } ?: 1

        circularProgressIndicator.max = durationSecs
        circularProgressIndicator.setProgressCompat(currentPositionSecs, true)

        if (playbackStatus.mediaItem != null) {
            slideUp()
        } else {
            slideDown()
        }
    }
}
