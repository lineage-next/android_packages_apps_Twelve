/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Context.permissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.permissionsGranted(permissions: Array<String>) = permissions.all {
    permissionGranted(it)
}
