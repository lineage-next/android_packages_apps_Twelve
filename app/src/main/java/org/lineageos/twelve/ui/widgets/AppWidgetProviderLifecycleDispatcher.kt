/*
 * SPDX-FileCopyrightText: 2017 The Android Open Source Project
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.widgets

import android.app.Service
import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Helper class to dispatch lifecycle events for a AppWidgetProvider.
 *
 * @param provider [LifecycleOwner] for a service, usually it is a provider itself
 */
class AppWidgetProviderLifecycleDispatcher(provider: LifecycleOwner) {
    private val registry: LifecycleRegistry = LifecycleRegistry(provider)
    @Suppress("DEPRECATION")
    private val handler: Handler = Handler()
    private var lastDispatchRunnable: DispatchRunnable? = null

    private fun postDispatchRunnable(event: Lifecycle.Event) {
        lastDispatchRunnable?.run()
        lastDispatchRunnable = DispatchRunnable(registry, event)
        handler.postAtFrontOfQueue(lastDispatchRunnable!!)
    }

    init {
        postDispatchRunnable(Lifecycle.Event.ON_CREATE)
    }

    /**
     * Must be a first call in [Service.onCreate] method, even before super.onCreate call.
     */
    fun onProviderPreSuperOnEnabled() {
        postDispatchRunnable(Lifecycle.Event.ON_START)
    }

    /**
     * Must be a first call in [Service.onBind] method, even before super.onBind
     * call.
     */
    fun onProviderPreSuperOnDisabled() {
        postDispatchRunnable(Lifecycle.Event.ON_STOP)
    }

    /**
     * [Lifecycle] for the given [LifecycleOwner]
     */
    val lifecycle: Lifecycle
        get() = registry

    internal class DispatchRunnable(
        private val registry: LifecycleRegistry,
        private val event: Lifecycle.Event
    ) : Runnable {
        private var wasExecuted = false

        override fun run() {
            if (!wasExecuted) {
                registry.handleLifecycleEvent(event)
                wasExecuted = true
            }
        }
    }
}
