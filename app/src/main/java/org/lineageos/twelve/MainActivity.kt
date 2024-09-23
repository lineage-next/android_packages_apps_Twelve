/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import kotlin.reflect.cast

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    // NavController
    private val navHostFragment by lazy {
        NavHostFragment::class.cast(
            supportFragmentManager.findFragmentById(R.id.navHostFragment)
        )
    }
    private val navController by lazy { navHostFragment.navController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        enableEdgeToEdge()

        // Handle now playing
        if (intent.getBooleanExtra(EXTRA_OPEN_NOW_PLAYING, false)) {
            // If we're playing any media, we must've the required permissions to do so
            navController.navigate(R.id.fragment_now_playing)
        }
    }

    companion object {
        /**
         * Open now playing fragment.
         * Type: [Boolean]
         */
        const val EXTRA_OPEN_NOW_PLAYING = "extra_now_playing"
    }
}
