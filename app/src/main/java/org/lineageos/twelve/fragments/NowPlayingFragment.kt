/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.models.RepeatMode
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.utils.TimestampFormatter
import org.lineageos.twelve.viewmodels.NowPlayingViewModel

/**
 * Now playing fragment.
 */
class NowPlayingFragment : Fragment(R.layout.fragment_now_playing) {
    // View models
    private val viewModel by viewModels<NowPlayingViewModel>()

    // Views
    private val albumArtImageView by getViewProperty<ImageView>(R.id.albumArtImageView)
    private val albumTitleTextView by getViewProperty<TextView>(R.id.albumTitleTextView)
    private val audioTitleTextView by getViewProperty<TextView>(R.id.audioTitleTextView)
    private val artistNameTextView by getViewProperty<TextView>(R.id.artistNameTextView)
    private val castImageButton by getViewProperty<ImageButton>(R.id.castImageButton)
    private val currentTimestampTextView by getViewProperty<TextView>(R.id.currentTimestampTextView)
    private val durationTimestampTextView by getViewProperty<TextView>(R.id.durationTimestampTextView)
    private val equalizerImageButton by getViewProperty<ImageButton>(R.id.equalizerImageButton)
    private val fileTypeMaterialCardView by getViewProperty<MaterialCardView>(R.id.fileTypeMaterialCardView)
    private val fileTypeTextView by getViewProperty<TextView>(R.id.fileTypeTextView)
    private val hideImageButton by getViewProperty<ImageButton>(R.id.hideImageButton)
    private val moreImageButton by getViewProperty<ImageButton>(R.id.moreImageButton)
    private val nextTrackImageButton by getViewProperty<ImageButton>(R.id.nextTrackImageButton)
    private val playPauseImageButton by getViewProperty<ImageButton>(R.id.playPauseImageButton)
    private val playlistNameTextView by getViewProperty<TextView>(R.id.playlistNameTextView)
    private val previousTrackImageButton by getViewProperty<ImageButton>(R.id.previousTrackImageButton)
    private val progressSlider by getViewProperty<Slider>(R.id.progressSlider)
    private val repeatImageButton by getViewProperty<ImageButton>(R.id.repeatImageButton)
    private val repeatMarkerImageButton by getViewProperty<ImageButton>(R.id.repeatMarkerImageButton)
    private val shuffleImageButton by getViewProperty<ImageButton>(R.id.shuffleImageButton)
    private val shuffleMarkerImageButton by getViewProperty<ImageButton>(R.id.shuffleMarkerImageButton)

    // Progress slider state
    private var isProgressSliderDragging = false
    private var animator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Top bar
        hideImageButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Media controls
        progressSlider.valueFrom = 0f
        progressSlider.setLabelFormatter {
            TimestampFormatter.formatTimestampMillis(it)
        }
        progressSlider.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    isProgressSliderDragging = true
                    animator?.cancel()
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    isProgressSliderDragging = false
                    viewModel.seekToPosition(slider.value.toLong())
                }
            }
        )

        previousTrackImageButton.setOnClickListener {
            viewModel.seekToPrevious()
        }

        playPauseImageButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        nextTrackImageButton.setOnClickListener {
            viewModel.seekToNext()
        }

        // Bottom bar buttons
        shuffleImageButton.setOnClickListener {
            viewModel.toggleShuffleMode()
        }

        repeatImageButton.setOnClickListener {
            viewModel.toggleRepeatMode()
        }

        equalizerImageButton.setOnClickListener {
            val activity = requireActivity()

            // Open system equalizer
            val intent = Intent.createChooser(
                Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, activity.packageName)
                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                },
                null
            )

            activity.startActivity(intent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playbackStatus.collectLatest {
                        when (it) {
                            is RequestStatus.Loading -> {
                                // Do nothing
                            }

                            is RequestStatus.Success -> {
                                val playbackStatus = it.data

                                playbackStatus.mediaItem?.localConfiguration?.mimeType
                                    ?.takeIf { mimeType -> mimeType.contains('/') }
                                    ?.substringAfterLast('/')
                                    ?.also {
                                        fileTypeTextView.text = it
                                        fileTypeMaterialCardView.isVisible = true
                                    } ?: run {
                                    fileTypeMaterialCardView.isVisible = false
                                }

                                playbackStatus.mediaMetadata.artworkData?.also { artworkData ->
                                    BitmapFactory.decodeByteArray(
                                        artworkData, 0, artworkData.size
                                    )?.let { bitmap ->
                                        albumArtImageView.setImageBitmap(bitmap)
                                    }
                                } ?: playbackStatus.mediaMetadata.artworkUri?.also { artworkUri ->
                                    ImageDecoder.createSource(
                                        requireContext().contentResolver,
                                        artworkUri
                                    ).let { source ->
                                        ImageDecoder.decodeBitmap(source)
                                    }.also { bitmap ->
                                        albumArtImageView.setImageBitmap(bitmap)
                                    }
                                } ?: albumArtImageView.setImageResource(R.drawable.ic_music_note)

                                val audioTitle = playbackStatus.mediaMetadata.displayTitle
                                    ?: playbackStatus.mediaMetadata.title
                                audioTitle?.let { title ->
                                    audioTitleTextView.text = title
                                    audioTitleTextView.isVisible = true
                                } ?: run {
                                    audioTitleTextView.isVisible = false
                                }

                                playbackStatus.mediaMetadata.artist?.let { artist ->
                                    artistNameTextView.text = artist
                                    artistNameTextView.isVisible = true
                                } ?: run {
                                    artistNameTextView.isVisible = false
                                }

                                playbackStatus.mediaMetadata.albumTitle?.let { albumTitle ->
                                    albumTitleTextView.text = albumTitle
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

                                shuffleMarkerImageButton.isVisible =
                                    playbackStatus.shuffleModeEnabled

                                repeatImageButton.setImageResource(
                                    when (playbackStatus.repeatMode) {
                                        RepeatMode.NONE,
                                        RepeatMode.ALL -> R.drawable.ic_repeat

                                        RepeatMode.ONE -> R.drawable.ic_repeat_one
                                    }
                                )
                                repeatMarkerImageButton.isVisible =
                                    playbackStatus.repeatMode != RepeatMode.NONE
                            }

                            is RequestStatus.Error -> throw Exception(
                                "Error while getting playback status"
                            )
                        }
                    }
                }

                launch {
                    // Restart animation based on this value being changed
                    var oldValue = 0f

                    viewModel.durationCurrentPositionMs.collectLatest { durationCurrentPositionMs ->
                        val (durationMs, currentPositionMs, playbackSpeed) =
                            durationCurrentPositionMs.let {
                                Triple(
                                    it.first ?: 0L,
                                    it.second ?: 0L,
                                    it.third
                                )
                            }

                        // We want to lose ms precision with the slider
                        val durationSecs = durationMs / 1000
                        val currentPositionSecs = currentPositionMs / 1000

                        val newValueTo = (durationSecs * 1000).toFloat().takeIf { it > 0 } ?: 1f
                        val newValue = (currentPositionSecs * 1000).toFloat()

                        val valueToChanged = progressSlider.valueTo != newValueTo

                        // Only +1s should be animated
                        val shouldBeAnimated = (newValue - oldValue) == 1000f

                        val newAnimator = ValueAnimator.ofFloat(
                            progressSlider.value, newValue
                        ).apply {
                            interpolator = LinearInterpolator()
                            duration = 1000 / playbackSpeed.toLong()
                            doOnStart {
                                // Update valueTo at the start of the animation
                                if (progressSlider.valueTo != newValueTo) {
                                    progressSlider.valueTo = newValueTo
                                }
                            }
                            addUpdateListener {
                                // Clamp the new value up to valueTo
                                progressSlider.value = (it.animatedValue as Float).coerceAtMost(
                                    progressSlider.valueTo
                                )
                            }
                        }

                        if (!isProgressSliderDragging || valueToChanged) {
                            animator?.also { oldAnimator ->
                                // We want to start a new animation only if the current duration
                                // changed
                                if (oldValue != newValue) {
                                    oldValue = newValue

                                    // Start the new animation right after old one finishes
                                    oldAnimator.doOnEnd {
                                        if (shouldBeAnimated) {
                                            animator = newAnimator
                                            newAnimator.start()
                                        } else {
                                            animator = null
                                            // Clamp the new value up to valueTo
                                            progressSlider.value = newValue.coerceAtMost(
                                                progressSlider.valueTo
                                            )
                                        }
                                    }

                                    if (oldAnimator.isRunning) {
                                        oldAnimator.cancel()
                                    } else {
                                        oldAnimator.end()
                                    }
                                }
                            } ?: run {
                                // This is the first animation
                                animator = newAnimator
                                newAnimator.start()
                            }
                        }

                        currentTimestampTextView.text = TimestampFormatter.formatTimestampMillis(
                            currentPositionMs
                        )
                        durationTimestampTextView.text = TimestampFormatter.formatTimestampMillis(
                            durationMs
                        )
                    }
                }
            }
        }
    }
}
