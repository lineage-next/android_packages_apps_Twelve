/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.TwelveApplication
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.models.RepeatMode
import org.lineageos.twelve.utils.TimestampFormatter
import org.lineageos.twelve.viewmodels.NowPlayingViewModel
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Now playing fragment.
 */
class NowPlayingFragment : Fragment(R.layout.fragment_now_playing) {
    // View models
    private val viewModel by viewModels<NowPlayingViewModel>()

    // Views
    private val addOrRemoveFromPlaylistsMaterialButton by getViewProperty<MaterialButton>(R.id.addOrRemoveFromPlaylistsMaterialButton)
    private val albumArtImageView by getViewProperty<ImageView>(R.id.albumArtImageView)
    private val albumTitleTextView by getViewProperty<TextView>(R.id.albumTitleTextView)
    private val audioTitleTextView by getViewProperty<TextView>(R.id.audioTitleTextView)
    private val artistNameTextView by getViewProperty<TextView>(R.id.artistNameTextView)
    private val castMaterialButton by getViewProperty<MaterialButton>(R.id.castMaterialButton)
    private val currentTimestampTextView by getViewProperty<TextView>(R.id.currentTimestampTextView)
    private val durationTimestampTextView by getViewProperty<TextView>(R.id.durationTimestampTextView)
    private val equalizerMaterialButton by getViewProperty<MaterialButton>(R.id.equalizerMaterialButton)
    private val fileTypeMaterialCardView by getViewProperty<MaterialCardView>(R.id.fileTypeMaterialCardView)
    private val fileTypeTextView by getViewProperty<TextView>(R.id.fileTypeTextView)
    private val moreMaterialButton by getViewProperty<MaterialButton>(R.id.moreMaterialButton)
    private val nestedScrollView by getViewProperty<NestedScrollView>(R.id.nestedScrollView)
    private val nextTrackMaterialButton by getViewProperty<MaterialButton>(R.id.nextTrackMaterialButton)
    private val playPauseMaterialButton by getViewProperty<MaterialButton>(R.id.playPauseMaterialButton)
    private val playbackSpeedMaterialButton by getViewProperty<MaterialButton>(R.id.playbackSpeedMaterialButton)
    private val previousTrackMaterialButton by getViewProperty<MaterialButton>(R.id.previousTrackMaterialButton)
    private val progressSlider by getViewProperty<Slider>(R.id.progressSlider)
    private val repeatMarkerImageView by getViewProperty<ImageView>(R.id.repeatMarkerImageView)
    private val repeatMaterialButton by getViewProperty<MaterialButton>(R.id.repeatMaterialButton)
    private val shuffleMarkerImageView by getViewProperty<ImageView>(R.id.shuffleMarkerImageView)
    private val shuffleMaterialButton by getViewProperty<MaterialButton>(R.id.shuffleMaterialButton)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)

    // Progress slider state
    private var isProgressSliderDragging = false
    private var animator: ValueAnimator? = null

    // AudioFX
    private val audioEffectsStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Empty
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(nestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                bottom = insets.bottom,
            )

            windowInsets
        }

        // Top bar
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        fileTypeMaterialCardView.setOnClickListener {
            findNavController().navigate(
                R.id.action_nowPlayingFragment_to_fragment_now_playing_stats_dialog
            )
        }

        // Audio informations
        audioTitleTextView.isSelected = true
        artistNameTextView.isSelected = true
        albumTitleTextView.isSelected = true

        // Media controls
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
                    viewModel.seekToPosition(slider.value.roundToLong())
                }
            }
        )

        shuffleMaterialButton.setOnClickListener {
            viewModel.toggleShuffleMode()
        }

        previousTrackMaterialButton.setOnClickListener {
            viewModel.seekToPrevious()
        }

        playPauseMaterialButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        nextTrackMaterialButton.setOnClickListener {
            viewModel.seekToNext()
        }

        repeatMaterialButton.setOnClickListener {
            viewModel.toggleRepeatMode()
        }

        // Bottom bar buttons
        playbackSpeedMaterialButton.setOnClickListener {
            viewModel.shufflePlaybackSpeed()
        }

        equalizerMaterialButton.setOnClickListener {
            val activity = requireActivity()

            // Open system equalizer
            audioEffectsStartForResult.launch(
                Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, activity.packageName)
                    putExtra(
                        AudioEffect.EXTRA_AUDIO_SESSION,
                        (activity.application as TwelveApplication).audioSessionId
                    )
                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                },
                null
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isPlaying.collectLatest { isPlaying ->
                        playPauseMaterialButton.setIconResource(
                            when (isPlaying) {
                                true -> R.drawable.ic_pause
                                false -> R.drawable.ic_play_arrow
                            }
                        )
                    }
                }

                launch {
                    viewModel.mediaItem.collectLatest { mediaItem ->
                        addOrRemoveFromPlaylistsMaterialButton.setOnClickListener {
                            mediaItem?.localConfiguration?.uri?.let { uri ->
                                findNavController().navigate(
                                    R.id.action_nowPlayingFragment_to_fragment_add_or_remove_from_playlists,
                                    AddOrRemoveFromPlaylistsFragment.createBundle(uri)
                                )
                            }
                        }
                    }
                }

                launch {
                    viewModel.mediaMetadata.collectLatest { mediaMetadata ->
                        mediaMetadata.artworkData?.also { artworkData ->
                            BitmapFactory.decodeByteArray(
                                artworkData, 0, artworkData.size
                            )?.let { bitmap ->
                                albumArtImageView.setImageBitmap(bitmap)
                            }
                        } ?: mediaMetadata.artworkUri?.also { artworkUri ->
                            ImageDecoder.createSource(
                                requireContext().contentResolver,
                                artworkUri
                            ).let { source ->
                                ImageDecoder.decodeBitmap(source)
                            }.also { bitmap ->
                                albumArtImageView.setImageBitmap(bitmap)
                            }
                        } ?: albumArtImageView.setImageResource(R.drawable.ic_music_note)

                        val audioTitle = mediaMetadata.displayTitle
                            ?: mediaMetadata.title
                        audioTitle?.let { title ->
                            if (audioTitleTextView.text != title) {
                                audioTitleTextView.text = title
                            }
                            audioTitleTextView.isVisible = true
                        } ?: run {
                            audioTitleTextView.isVisible = false
                        }

                        mediaMetadata.artist?.let { artist ->
                            if (artistNameTextView.text != artist) {
                                artistNameTextView.text = artist
                            }
                            artistNameTextView.isVisible = true
                        } ?: run {
                            artistNameTextView.isVisible = false
                        }

                        mediaMetadata.albumTitle?.let { albumTitle ->
                            if (albumTitleTextView.text != albumTitle) {
                                albumTitleTextView.text = albumTitle
                            }
                            albumTitleTextView.isVisible = true
                        } ?: run {
                            albumTitleTextView.isVisible = false
                        }
                    }
                }

                launch {
                    viewModel.playbackParameters.collectLatest {
                        it?.also {
                            playbackSpeedMaterialButton.text = getString(
                                R.string.playback_speed_format,
                                playbackSpeedFormatter.format(it.speed),
                            )
                        }
                    }
                }

                launch {
                    viewModel.displayFileType.collectLatest {
                        it?.let { displayFileType ->
                            fileTypeTextView.text = displayFileType
                            fileTypeMaterialCardView.isVisible = true
                        } ?: run {
                            fileTypeMaterialCardView.isVisible = false
                        }
                    }
                }

                launch {
                    viewModel.repeatMode.collectLatest {
                        repeatMaterialButton.setIconResource(
                            when (it) {
                                RepeatMode.NONE,
                                RepeatMode.ALL -> R.drawable.ic_repeat

                                RepeatMode.ONE -> R.drawable.ic_repeat_one
                            }
                        )
                        repeatMarkerImageView.isVisible = it != RepeatMode.NONE
                    }
                }

                launch {
                    viewModel.shuffleMode.collectLatest { shuffleModeEnabled ->
                        shuffleMarkerImageView.isVisible = shuffleModeEnabled
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
                        val valueChanged = oldValue != newValue

                        // Only +1s should be animated
                        val shouldBeAnimated = (newValue - oldValue) == 1000f

                        val newAnimator = ValueAnimator.ofFloat(
                            progressSlider.value, newValue
                        ).apply {
                            interpolator = LinearInterpolator()
                            duration = 1000 / playbackSpeed.roundToLong()
                            doOnStart {
                                // Update valueTo at the start of the animation
                                if (progressSlider.valueTo != newValueTo) {
                                    progressSlider.valueTo = newValueTo
                                }
                            }
                            addUpdateListener {
                                progressSlider.value = (it.animatedValue as Float)
                            }
                        }

                        oldValue = newValue

                        /**
                         * Update only if:
                         * - The value changed and the user isn't dragging the slider
                         * - valueTo changed
                         */
                        if ((!isProgressSliderDragging && valueChanged) || valueToChanged) {
                            val afterOldAnimatorEnded = {
                                if (shouldBeAnimated) {
                                    animator = newAnimator
                                    newAnimator.start()
                                } else {
                                    animator = null
                                    // Update both valueTo and value
                                    progressSlider.valueTo = newValueTo
                                    progressSlider.value = newValue
                                }
                            }

                            animator?.also { oldAnimator ->
                                // Start the new animation right after old one finishes
                                oldAnimator.doOnEnd {
                                    afterOldAnimatorEnded()
                                }

                                if (oldAnimator.isRunning) {
                                    oldAnimator.cancel()
                                } else {
                                    oldAnimator.end()
                                }
                            } ?: run {
                                // This is the first animation
                                afterOldAnimatorEnded()
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

                launch {
                    viewModel.availableCommands().collectLatest {
                        it?.let {
                            playbackSpeedMaterialButton.isVisible =
                                it.contains(Player.COMMAND_SET_SPEED_AND_PITCH)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        animator?.cancel()
        animator = null

        super.onDestroyView()
    }

    companion object {
        private val decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT)

        private val playbackSpeedFormatter = DecimalFormat("0.#", decimalFormatSymbols)
    }
}
