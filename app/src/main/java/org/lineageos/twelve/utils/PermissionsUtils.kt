/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.Manifest
import android.os.Build

/**
 * App's permissions utils.
 */
object PermissionsUtils {
    /**
     * Permissions required to run the app
     */
    val mainPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_AUDIO)
            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        add(Manifest.permission.ACCESS_MEDIA_LOCATION)
    }.toTypedArray()
}
