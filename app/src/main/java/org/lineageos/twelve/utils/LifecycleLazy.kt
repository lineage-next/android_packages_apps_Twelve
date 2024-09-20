/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * [Lazy] but lifecycle aware.
 *
 * @param lifecycle The lifecycle to observe
 * @param lifecycleState The [Lifecycle] [Lifecycle.State] on which the value is valid. When the
 *   lifecycle is outside of this state, the value will become null.
 */
class LifecycleLazy<T : Any?>(
    private val lifecycle: Lifecycle,
    private val lifecycleState: Lifecycle.State,
    initializer: () -> T
) : BaseLazy<T>(initializer), LifecycleEventObserver {
    override fun onValueInitialized(value: T) {
        super.onValueInitialized(value)

        lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val newState = event.targetState

        if (!newState.isAtLeast(lifecycleState)) {
            cachedValue = UNINITIALIZED_VALUE
            source.lifecycle.removeObserver(this)
        }
    }
}
