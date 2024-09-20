/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * [Lazy] but lifecycle aware, [Fragment]'s view's lifecycle edition.
 *
 * @param viewLifecycleOwnerLiveData The [Fragment]'s view's lifecycle owner [LiveData]
 * @param lifecycleState The [Lifecycle] [Lifecycle.State] on which the value is valid. When the
 *   lifecycle is outside of this state, the value will become null.
 */
class ViewLifecycleLazy<T : Any?>(
    private val viewLifecycleOwnerLiveData: LiveData<LifecycleOwner>,
    private val lifecycleState: Lifecycle.State,
    initializer: () -> T
) : BaseLazy<T>(initializer), LifecycleEventObserver {
    override fun onValueInitialized(value: T) {
        super.onValueInitialized(value)

        viewLifecycleOwnerLiveData.value!!.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val newState = event.targetState

        if (!newState.isAtLeast(lifecycleState)) {
            cachedValue = UNINITIALIZED_VALUE
            source.lifecycle.removeObserver(this)
        }
    }
}
