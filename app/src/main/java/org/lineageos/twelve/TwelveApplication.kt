/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.app.Application
import com.google.android.material.color.DynamicColors
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.repositories.MediaRepository

class TwelveApplication : Application() {
    private val database by lazy { TwelveDatabase.getInstance(this) }
    val mediaRepository by lazy { MediaRepository(this, database) }

    override fun onCreate() {
        super.onCreate()

        // Observe dynamic colors changes
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
