/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getParcelable
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.setProgressCompat
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.recyclerview.UniqueItemDiffCallback
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.utils.PermissionsChecker
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.utils.TimestampFormatter
import org.lineageos.twelve.viewmodels.AlbumViewModel

/**
 * Single music album viewer.
 */
class AlbumFragment : Fragment(R.layout.fragment_album) {
    // View models
    private val viewModel by viewModels<AlbumViewModel>()

    // Views
    private val albumTitleTextView by getViewProperty<TextView>(R.id.albumTitleTextView)
    private val artistNameTextView by getViewProperty<TextView>(R.id.artistNameTextView)
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val noElementsNestedScrollView by getViewProperty<NestedScrollView>(R.id.noElementsNestedScrollView)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)
    private val thumbnailImageView by getViewProperty<ImageView>(R.id.thumbnailImageView)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)
    private val tracksInfoTextView by getViewProperty<TextView>(R.id.tracksInfoTextView)
    private val yearTextView by getViewProperty<TextView>(R.id.yearTextView)

    // Recyclerview
    private val adapter by lazy {
        object : SimpleListAdapter<Audio, ListItem>(
            UniqueItemDiffCallback(),
            ::ListItem,
        ) {
            override fun ViewHolder.onPrepareView() {
                view.setLeadingIconImage(R.drawable.ic_music_note)
                view.setOnClickListener {
                    item?.let {
                        viewModel.playAudio(currentList, bindingAdapterPosition)

                        findNavController().navigate(
                            R.id.action_albumFragment_to_fragment_now_playing
                        )
                    }
                }
                view.setOnLongClickListener {
                    item?.let {
                        findNavController().navigate(
                            R.id.action_albumFragment_to_fragment_audio_bottom_sheet_dialog,
                            AudioBottomSheetDialogFragment.createBundle(
                                it.uri,
                                fromAlbum = true,
                            )
                        )
                    }

                    true
                }
            }

            override fun ViewHolder.onBindView(item: Audio) {
                view.headlineText = item.title
                view.supportingText = item.artistName
                view.trailingSupportingText = TimestampFormatter.formatTimestampMillis(
                    item.durationMs
                )
            }
        }
    }

    // Arguments
    private val albumUri: Uri
        get() = requireArguments().getParcelable(ARG_ALBUM_URI, Uri::class)!!

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom,
            )

            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(noElementsNestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                left = insets.left,
                right = insets.right,
                bottom = insets.bottom,
            )

            windowInsets
        }

        toolbar.setupWithNavController(findNavController())

        recyclerView.adapter = adapter

        viewModel.loadAlbum(albumUri)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null

        super.onDestroyView()
    }

    private suspend fun loadData() {
        viewModel.album.collectLatest {
            linearProgressIndicator.setProgressCompat(it, true)

            when (it) {
                is RequestStatus.Loading -> {
                    // Do nothing
                }

                is RequestStatus.Success -> {
                    val (album, audios) = it.data

                    toolbar.title = album.title
                    albumTitleTextView.text = album.title

                    album.thumbnail?.uri?.also { uri ->
                        thumbnailImageView.load(uri)
                    } ?: album.thumbnail?.bitmap?.also { bitmap ->
                        thumbnailImageView.load(bitmap)
                    } ?: thumbnailImageView.setImageResource(R.drawable.ic_album)

                    artistNameTextView.text = album.artistName
                    artistNameTextView.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_albumFragment_to_fragment_artist,
                            ArtistFragment.createBundle(album.artistUri)
                        )
                    }

                    album.year?.also { year ->
                        yearTextView.isVisible = true
                        yearTextView.text = year.toString()
                    } ?: run {
                        yearTextView.isVisible = false
                    }

                    val totalDurationMs = audios.sumOf { audio ->
                        audio.durationMs
                    }
                    val totalDurationMinutes = totalDurationMs / 1000 / 60

                    val tracksCount = resources.getQuantityString(
                        R.plurals.tracks_count,
                        audios.size,
                        audios.size
                    )
                    val tracksDuration = resources.getQuantityString(
                        R.plurals.tracks_duration,
                        totalDurationMinutes,
                        totalDurationMinutes
                    )
                    tracksInfoTextView.text = getString(
                        R.string.tracks_info,
                        tracksCount, tracksDuration
                    )

                    adapter.submitList(audios)

                    val isEmpty = audios.isEmpty()
                    recyclerView.isVisible = !isEmpty
                    noElementsNestedScrollView.isVisible = isEmpty
                }

                is RequestStatus.Error -> {
                    Log.e(LOG_TAG, "Error loading album, error: ${it.type}")

                    toolbar.title = ""
                    albumTitleTextView.text = ""

                    adapter.submitList(listOf())

                    recyclerView.isVisible = false
                    noElementsNestedScrollView.isVisible = true

                    if (it.type == RequestStatus.Error.Type.NOT_FOUND) {
                        // Get out of here
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = AlbumFragment::class.simpleName!!

        private const val ARG_ALBUM_URI = "album_uri"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param albumUri The URI of the album to display
         */
        fun createBundle(
            albumUri: Uri,
        ) = bundleOf(
            ARG_ALBUM_URI to albumUri,
        )
    }
}
