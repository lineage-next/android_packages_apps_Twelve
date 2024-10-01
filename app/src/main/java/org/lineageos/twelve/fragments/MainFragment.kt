/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ui.views.NowPlayingBar
import org.lineageos.twelve.viewmodels.NowPlayingViewModel

/**
 * The home page.
 */
@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {
    // View models
    private val viewModel by viewModels<NowPlayingViewModel>()

    // Views
    private val bottomNavigationView by getViewProperty<BottomNavigationView>(R.id.bottomNavigationView)
    private val nowPlayingBar by getViewProperty<NowPlayingBar>(R.id.nowPlayingBar)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)
    private val viewPager2 by getViewProperty<ViewPager2>(R.id.viewPager2)

    // ViewPager2
    private val onPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setupWithNavController(findNavController())

        viewPager2.isUserInputEnabled = false
        viewPager2.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]()
        }
        viewPager2.offscreenPageLimit = fragments.size
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.activityFragment -> {
                    viewPager2.currentItem = 0
                    true
                }

                R.id.searchFragment -> {
                    viewPager2.currentItem = 1
                    true
                }

                R.id.libraryFragment -> {
                    viewPager2.currentItem = 2
                    true
                }

                else -> false
            }
        }

        nowPlayingBar.setOnPlayPauseClickListener {
            viewModel.togglePlayPause()
        }

        nowPlayingBar.setOnNowPlayingClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_fragment_now_playing)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.durationCurrentPositionMs.collectLatest {
                        nowPlayingBar.updateDurationCurrentPositionMs(it.first, it.second)
                    }
                }

                launch {
                    viewModel.isPlaying.collectLatest {
                        nowPlayingBar.updateIsPlaying(it)
                    }
                }

                launch {
                    viewModel.mediaItem.collectLatest {
                        nowPlayingBar.updateMediaItem(it)
                    }
                }

                launch {
                    viewModel.mediaMetadata.collectLatest {
                        nowPlayingBar.updateMediaMetadata(it)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        viewPager2.adapter = null

        super.onDestroyView()
    }

    companion object {
        // Keep in sync with the BottomNavigationView menu
        private val fragments = arrayOf(
            { ActivityFragment() },
            { SearchFragment() },
            { LibraryFragment() },
        )
    }
}
