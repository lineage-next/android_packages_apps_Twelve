/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.PixelFormat
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.bogerchan.niervisualizer.NierVisualizerManager
import org.lineageos.twelve.R
import org.lineageos.twelve.TwelveApplication
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.models.PlaybackState
import org.lineageos.twelve.models.RepeatMode
import org.lineageos.twelve.models.RequestStatus
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
    private val albumArtConstraintLayout by getViewProperty<ConstraintLayout?>(R.id.albumArtConstraintLayout)
    private val albumArtImageView by getViewProperty<ImageView>(R.id.albumArtImageView)
    private val albumTitleTextView by getViewProperty<TextView>(R.id.albumTitleTextView)
    private val audioTitleTextView by getViewProperty<TextView>(R.id.audioTitleTextView)
    private val artistNameTextView by getViewProperty<TextView>(R.id.artistNameTextView)
    private val currentTimestampTextView by getViewProperty<TextView>(R.id.currentTimestampTextView)
    private val durationTimestampTextView by getViewProperty<TextView>(R.id.durationTimestampTextView)
    private val equalizerMaterialButton by getViewProperty<MaterialButton>(R.id.equalizerMaterialButton)
    private val fileTypeMaterialCardView by getViewProperty<MaterialCardView>(R.id.fileTypeMaterialCardView)
    private val fileTypeTextView by getViewProperty<TextView>(R.id.fileTypeTextView)
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
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
    private val visualizerMaterialButton by getViewProperty<MaterialButton>(R.id.visualizerMaterialButton)
    private val visualizerSurfaceView by getViewProperty<SurfaceView>(R.id.visualizerSurfaceView)

    // Progress slider state
    private var isProgressSliderDragging = false
    private var animator: ValueAnimator? = null

    // AudioFX
    private val audioSessionId: Int
        get() = (requireActivity().application as TwelveApplication).audioSessionId
    private val audioEffectsStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Empty
        }

    // Visualizer
    private val visualizerManager = NierVisualizerManager()
    private val visualizerViewLifecycleObserver = object : DefaultLifecycleObserver {
        private var isVisualizerInitialized = false
        private var isVisualizerStarted = false

        override fun onCreate(owner: LifecycleOwner) {
            val initResult = visualizerManager.init(audioSessionId)
            isVisualizerInitialized = initResult == NierVisualizerManager.SUCCESS

            owner.lifecycleScope.launch {
                owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.currentVisualizerType.collectLatest { currentVisualizerType ->
                        if (isVisualizerInitialized) {
                            currentVisualizerType.factory.invoke()?.let {
                                visualizerManager.start(visualizerSurfaceView, it)
                                isVisualizerStarted = true
                            } ?: run {
                                visualizerManager.stop()
                                isVisualizerStarted = false
                            }
                        }
                    }
                }
            }
        }

        override fun onResume(owner: LifecycleOwner) {
            if (isVisualizerStarted) {
                visualizerManager.resume()
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            if (isVisualizerStarted) {
                visualizerManager.pause()
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            if (isVisualizerStarted) {
                visualizerManager.stop()
            }
            isVisualizerStarted = false
        }

        override fun onDestroy(owner: LifecycleOwner) {
            if (isVisualizerInitialized) {
                visualizerManager.release()
            }
            isVisualizerInitialized = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Insets
        albumArtConstraintLayout?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                v.updatePadding(
                    left = insets.left,
                    bottom = insets.bottom,
                )

                windowInsets
            }
        }

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

        // Visualizer
        visualizerSurfaceView.setZOrderOnTop(true)
        visualizerSurfaceView.holder.setFormat(PixelFormat.TRANSPARENT)

        viewLifecycleOwner.lifecycle.addObserver(visualizerViewLifecycleObserver)

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
            // Open system equalizer
            audioEffectsStartForResult.launch(
                Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                    putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().packageName)
                    putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
                    putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                },
                null
            )
        }

        visualizerMaterialButton.setOnClickListener {
            viewModel.nextVisualizerType()
        }
        visualizerMaterialButton.setOnLongClickListener {
            viewModel.disableVisualizer()
            true
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
                    viewModel.playbackState.collectLatest { playbackState ->
                        playbackState?.let {
                            linearProgressIndicator.isVisible = it == PlaybackState.BUFFERING
                        }
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
                    viewModel.mediaArtwork.collectLatest {
                        when (it) {
                            is RequestStatus.Loading -> {
                                // Do nothing
                            }

                            is RequestStatus.Success -> {
                                it.data?.bitmap?.also { bitmap ->
                                    albumArtImageView.setImageBitmap(bitmap)
                                } ?: it.data?.uri?.also { artworkUri ->
                                    ImageDecoder.createSource(
                                        requireContext().contentResolver,
                                        artworkUri
                                    ).let { source ->
                                        ImageDecoder.decodeBitmap(source)
                                    }.also { bitmap ->
                                        albumArtImageView.setImageBitmap(bitmap)
                                    }
                                } ?: albumArtImageView.setImageResource(R.drawable.ic_music_note)
                            }

                            is RequestStatus.Error -> throw Exception(
                                "Error while getting media artwork"
                            )
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
                    viewModel.availableCommands.collectLatest {
                        it?.let {
                            shuffleMaterialButton.isEnabled = it.contains(
                                Player.COMMAND_SET_SHUFFLE_MODE
                            )

                            previousTrackMaterialButton.isEnabled = it.contains(
                                Player.COMMAND_SEEK_TO_PREVIOUS
                            )

                            playPauseMaterialButton.isEnabled = it.contains(
                                Player.COMMAND_PLAY_PAUSE
                            )

                            nextTrackMaterialButton.isEnabled = it.contains(
                                Player.COMMAND_SEEK_TO_NEXT
                            )

                            repeatMaterialButton.isEnabled = it.contains(
                                Player.COMMAND_SET_REPEAT_MODE
                            )

                            playbackSpeedMaterialButton.isEnabled = it.contains(
                                Player.COMMAND_SET_SPEED_AND_PITCH
                            )
                        }
                    }
                }

                launch {
                    viewModel.currentVisualizerType.collectLatest {
                        visualizerSurfaceView.isVisible =
                            it != NowPlayingViewModel.VisualizerType.NONE
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
