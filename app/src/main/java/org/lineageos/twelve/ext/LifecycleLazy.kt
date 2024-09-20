/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.fragment.app.Fragment

inline fun <reified T : Any> Fragment.lifecycleLazy(noinline initializer: () -> T) =
    LifecycleLazy(lifecycle, initializer)

class LifecycleLazy<T : Any>(
    private val lifecycle: Lifecycle,
    private val initializer: () -> T
) : Lazy<T>, LifecycleEventObserver {
    private var cached: T? = null

    override val value: T
        get() = cached ?: run {
            initializer().also {
                cached = it
                lifecycle.addObserver(this)
            }
        }

    override fun isInitialized() = cached != null

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> cached = null
            else -> {}
        }
    }
}
