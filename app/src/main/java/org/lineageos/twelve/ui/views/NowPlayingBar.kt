/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.graphics.ImageDecoder
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.slideDown
import org.lineageos.twelve.ext.slideUp
import org.lineageos.twelve.models.Thumbnail

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

    private var applyNavigationBarInsets = false

    init {
        inflate(context, R.layout.now_playing_bar, this)

        context.obtainStyledAttributes(attrs, R.styleable.NowPlayingBar, 0, 0).apply {
            try {
                applyNavigationBarInsets = getBoolean(
                    R.styleable.NowPlayingBar_applyNavigationBarInsets,
                    false
                )
            } finally {
                recycle()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            materialCardView.setContentPadding(
                0,
                0,
                0,
                when (applyNavigationBarInsets) {
                    true -> insets.bottom
                    false -> 0
                }
            )

            windowInsets
        }

        circularProgressIndicator.min = 0
    }

    fun setOnPlayPauseClickListener(l: OnClickListener?) =
        playPauseImageButton.setOnClickListener(l)

    fun setOnNowPlayingClickListener(l: OnClickListener?) {
        materialCardView.setOnClickListener(l)
    }

    fun updateIsPlaying(isPlaying: Boolean) {
        playPauseImageButton.setImageResource(
            when (isPlaying) {
                true -> R.drawable.ic_pause
                false -> R.drawable.ic_play_arrow
            }
        )
    }

    fun updateMediaItem(mediaItem: MediaItem?) {
        if (mediaItem != null) {
            slideUp()
        } else {
            slideDown()
        }
    }

    fun updateMediaMetadata(mediaMetadata: MediaMetadata) {
        mediaMetadata.title?.also {
            titleTextView.text = it
            titleTextView.isVisible = true
        } ?: run {
            titleTextView.isVisible = false
        }

        mediaMetadata.artist?.also {
            artistNameTextView.text = it
            artistNameTextView.isVisible = true
        } ?: run {
            artistNameTextView.isVisible = false
        }

        mediaMetadata.albumTitle?.also {
            albumTitleTextView.text = it
            albumTitleTextView.isVisible = true
        } ?: run {
            albumTitleTextView.isVisible = false
        }
    }

    fun updateMediaArtwork(artwork: Thumbnail?) {
        artwork?.bitmap?.also { bitmap ->
            thumbnailImageView.setImageBitmap(bitmap)
        } ?: artwork?.uri?.also { artworkUri ->
            ImageDecoder.createSource(
                context.contentResolver,
                artworkUri
            ).let { source ->
                ImageDecoder.decodeBitmap(source)
            }.also { bitmap ->
                thumbnailImageView.setImageBitmap(bitmap)
            }
        } ?: thumbnailImageView.setImageResource(R.drawable.ic_music_note)
    }

    fun updateDurationCurrentPositionMs(durationMs: Long?, currentPositionMs: Long?) {
        val currentPositionSecs = currentPositionMs?.let { it / 1000 }?.toInt() ?: 0
        val durationSecs = durationMs?.let { it / 1000 }?.toInt()?.takeIf { it != 0 } ?: 1

        circularProgressIndicator.max = durationSecs
        circularProgressIndicator.setProgressCompat(currentPositionSecs, true)
    }
}
