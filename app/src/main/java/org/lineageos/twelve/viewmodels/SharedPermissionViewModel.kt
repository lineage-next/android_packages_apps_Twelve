/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

class SharedPermissionViewModel : ViewModel() {
    // A shared flow to emit permission results
    private val _permissionResults = MutableSharedFlow<Map<String, Boolean>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val permissionResults: SharedFlow<Map<String, Boolean>> = _permissionResults

    // A state flow to track if a permission request is currently in progress
    private val _isRequestInProgress = MutableStateFlow(false)
    val isRequestInProgress: StateFlow<Boolean> = _isRequestInProgress

    // Function to request permissions
    fun requestPermissions(
        fragment: Fragment, permissions: Array<String>
    ) {
        // If a permission request is already in progress, return without doing anything
        if (_isRequestInProgress.getAndUpdate { true }) return

        // Filter out permissions that are already granted
        val neededPermissions = permissions.filterNot { permission ->
            ContextCompat.checkSelfPermission(
                fragment.requireContext(), permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        // If no permissions are needed, emit a result indicating all permissions are granted
        if (neededPermissions.isEmpty()) {
            emitPermissionResult(permissions.associateWith { true })
            _isRequestInProgress.update { false }
            return
        }

        // Register the callback for permission request
        val permissionLauncher: ActivityResultLauncher<Array<String>> =
            fragment.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                emitPermissionResult(result)
                // Reset the request in progress flag
                _isRequestInProgress.update { false }
            }

        // Request permissions
        permissionLauncher.launch(neededPermissions.toTypedArray())
    }

    // Function to emit permission results to the flow
    private fun emitPermissionResult(result: Map<String, Boolean>) {
        _permissionResults.tryEmit(result)
    }
}
