/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty

/**
 * The home page.
 */
class MainFragment : Fragment(R.layout.fragment_main) {
    // Views
    private val bottomNavigationView by getViewProperty<BottomNavigationView>(R.id.bottomNavigationView)
    private val nowPlayingFloatingActionButton by getViewProperty<FloatingActionButton>(R.id.nowPlayingFloatingActionButton)
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

        nowPlayingFloatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_fragment_now_playing)
        }
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
