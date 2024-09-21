/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.viewmodels.SharedPermissionViewModel

abstract class TwelveFragment(
    @LayoutRes val contentLayoutId: Int
) : Fragment(contentLayoutId) {
    // View models
    private val permissionViewModel by activityViewModels<SharedPermissionViewModel>()

    abstract fun loadData()

    fun setupPermissions() {
        // Collect permission results from the shared flow
        viewLifecycleOwner.lifecycleScope.launch {
            permissionViewModel.permissionResults.collectLatest { result ->
                if (result.all { it.value }) {
                    loadData()
                } else {
                    requireActivity().finish()
                }
            }
        }

        // Check if a permission request is in progress before requesting again
        viewLifecycleOwner.lifecycleScope.launch {
            permissionViewModel.isRequestInProgress.collectLatest { inProgress ->
                if (!inProgress) {
                    // Request permissions when needed
                    permissionViewModel.requestPermissions(
                        this@TwelveFragment, PermissionsUtils.mainPermissions
                    )
                }
            }
        }
    }
}
