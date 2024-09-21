/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.setProgressCompat
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.recyclerview.UniqueItemDiffCallback
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.viewmodels.PlaylistsViewModel
import org.lineageos.twelve.viewmodels.SharedPermissionViewModel

/**
 * View all music playlists.
 */
class PlaylistsFragment : TwelveFragment(R.layout.fragment_playlists) {
    // View models
    private val viewModel by viewModels<PlaylistsViewModel>()

    // Views
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val noElementsLinearLayout by getViewProperty<LinearLayout>(R.id.noElementsLinearLayout)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)

    // Recyclerview
    private val adapter = object : SimpleListAdapter<Playlist, ListItem>(
        UniqueItemDiffCallback(),
        ::ListItem,
    ) {
        override fun ViewHolder.onPrepareView() {
            view.setLeadingIconImage(R.drawable.ic_playlist_play)
            view.setOnClickListener {
                item?.let {
                    findNavController().navigate(
                        R.id.action_mainFragment_to_fragment_playlist,
                        PlaylistFragment.createBundle(it.uri)
                    )
                }
            }
        }

        override fun ViewHolder.onBindView(item: Playlist) {
            view.headlineText = item.name
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter

        setupPermissions()
    }

    override fun onDestroyView() {
        recyclerView.adapter = null

        super.onDestroyView()
    }

    override fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlists.collectLatest {
                    linearProgressIndicator.setProgressCompat(it, true)

                    when (it) {
                        is RequestStatus.Loading -> {
                            // Do nothing
                        }

                        is RequestStatus.Success -> {
                            adapter.submitList(it.data)

                            val isEmpty = it.data.isEmpty()
                            recyclerView.isVisible = !isEmpty
                            noElementsLinearLayout.isVisible = isEmpty
                        }

                        is RequestStatus.Error -> {
                            Log.e(LOG_TAG, "Failed to load playlists, error: ${it.type}")

                            adapter.submitList(emptyList())

                            recyclerView.isVisible = false
                            noElementsLinearLayout.isVisible = true
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = PlaylistsFragment::class.simpleName!!
    }
}
