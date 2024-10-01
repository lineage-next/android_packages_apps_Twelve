/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import com.google.android.material.color.DynamicColors
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.repositories.MediaRepository

@UnstableApi
class TwelveApplication : Application() {
    private val database by lazy { TwelveDatabase.getInstance(applicationContext) }
    val mediaRepository by lazy { MediaRepository(applicationContext, database) }
    val audioSessionId by lazy { Util.generateAudioSessionIdV21(applicationContext) }

    override fun onCreate() {
        super.onCreate()

        // Observe dynamic colors changes
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
