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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
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
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.utils.TimestampFormatter
import org.lineageos.twelve.viewmodels.AlbumViewModel
import org.lineageos.twelve.viewmodels.SharedPermissionViewModel

/**
 * Single music album viewer.
 */
class AlbumFragment : TwelveFragment(R.layout.fragment_album) {
    // View models
    private val viewModel by viewModels<AlbumViewModel>()

    // Views
    private val appBarLayout by getViewProperty<AppBarLayout>(R.id.appBarLayout)
    private val artistNameTextView by getViewProperty<TextView>(R.id.artistNameTextView)
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val noElementsLinearLayout by getViewProperty<LinearLayout>(R.id.noElementsLinearLayout)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setupWithNavController(findNavController())

        recyclerView.adapter = adapter

        viewModel.loadAlbum(albumUri)

        setupPermissions()
    }

    override fun onDestroyView() {
        recyclerView.adapter = null

        super.onDestroyView()
    }

    override fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.album.collectLatest {
                    linearProgressIndicator.setProgressCompat(it, true)

                    when (it) {
                        null -> {
                            adapter.submitList(listOf())

                            recyclerView.isVisible = false
                            noElementsLinearLayout.isVisible = false
                        }

                        is RequestStatus.Loading -> {
                            // Do nothing
                        }

                        is RequestStatus.Success -> {
                            val (album, audios) = it.data

                            toolbar.title = album.title

                            launch {
                                thumbnailImageView.setImageBitmap(album.thumbnail)
                            }

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

                            tracksInfoTextView.text = getString(
                                R.string.album_tracks_info,
                                audios.size, totalDurationMinutes
                            )

                            adapter.submitList(audios)

                            val isEmpty = audios.isEmpty()
                            recyclerView.isVisible = !isEmpty
                            noElementsLinearLayout.isVisible = isEmpty
                        }

                        is RequestStatus.Error -> {
                            Log.e(LOG_TAG, "Error loading album, error: ${it.type}")

                            toolbar.title = ""

                            adapter.submitList(listOf())

                            recyclerView.isVisible = false
                            noElementsLinearLayout.isVisible = true

                            if (it.type == RequestStatus.Error.Type.NOT_FOUND) {
                                // Get out of here
                                findNavController().navigateUp()
                            }
                        }
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
