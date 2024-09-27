/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-FileCopyrightText: 2020 Airbnb
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class ViewPropertyImpl<out T : View?>(
    private val fragment: Fragment,
    @IdRes private val viewId: Int,
) : Lazy<T> {
    private val clearViewHandler by lazy(LazyThreadSafetyMode.NONE) { Handler(Looper.getMainLooper()) }
    private var view: T? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
            viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    // Lifecycle listeners are called before onDestroyView in a Fragment.
                    // However, we want views to be able to use other views in onDestroyView
                    // to do cleanup so we clear the reference one frame later.
                    clearViewHandler.post { view = null }
                }
            })
        }
    }

    override val value: T
        get() = view ?: run {
            val lifecycle = fragment.viewLifecycleOwner.lifecycle
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                error("Cannot access view. View lifecycle is ${lifecycle.currentState}!")
            }

            fragment.requireView().findViewById<T>(viewId).also { view = it }
        }

    override fun isInitialized() = view != null
}

inline fun <reified T : View?> Fragment.getViewProperty(@IdRes viewId: Int): Lazy<T> =
    ViewPropertyImpl(this, viewId)
