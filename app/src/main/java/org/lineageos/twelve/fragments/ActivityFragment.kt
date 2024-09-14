/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.lineageos.twelve.R
import org.lineageos.twelve.utils.PermissionsGatedCallback
import org.lineageos.twelve.utils.PermissionsUtils

/**
 * User activity, notifications and recommendations.
 */
class ActivityFragment : Fragment(R.layout.fragment_activity) {
    // Permissions
    private val permissionsGatedCallback = PermissionsGatedCallback(
        this, PermissionsUtils.mainPermissions
    ) {
        // Do nothing
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionsGatedCallback.runAfterPermissionsCheck()
    }
}
