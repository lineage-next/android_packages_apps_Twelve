/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.permissionsGranted

/**
 * A class that checks main app permissions before starting the callback.
 */
class PermissionsGatedCallback private constructor(
    caller: ActivityResultCaller,
    private val getContext: () -> Context,
    private val getActivity: () -> Activity,
    private val permissions: Array<String>,
    private val callback: () -> Unit,
) {
    constructor(
        fragment: Fragment,
        permissions: Array<String>,
        callback: () -> Unit,
    ) : this(
        fragment,
        { fragment.requireContext() },
        { fragment.requireActivity() },
        permissions,
        callback,
    )

    constructor(
        activity: AppCompatActivity,
        permissions: Array<String>,
        callback: () -> Unit,
    ) : this(
        activity,
        { activity },
        { activity },
        permissions,
        callback,
    )

    private val activityResultLauncher = caller.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val context = getContext()

        if (it.isNotEmpty()) {
            if (!context.permissionsGranted(permissions)) {
                Toast.makeText(
                    context,
                    R.string.app_permissions_toast,
                    Toast.LENGTH_SHORT
                ).show()
                getActivity().finish()
            } else {
                callback()
            }
        }
    }

    fun runAfterPermissionsCheck() {
        activityResultLauncher.launch(permissions)
    }
}
