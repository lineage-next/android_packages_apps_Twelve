/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import org.lineageos.twelve.utils.LifecycleLazy
import org.lineageos.twelve.utils.ViewLifecycleLazy
import kotlin.properties.ReadOnlyProperty

inline fun <reified T : View?> getViewProperty(@IdRes viewId: Int) =
    ReadOnlyProperty<Fragment, T> { thisRef, _ ->
        thisRef.requireView().findViewById<T>(viewId)
    }

/**
 * @see LifecycleLazy
 */
inline fun <reified T : Any?> Fragment.lifecycleLazy(
    lifecycleState: Lifecycle.State,
    noinline initializer: () -> T
) = LifecycleLazy(lifecycle, lifecycleState, initializer)

/**
 * @see ViewLifecycleLazy
 */
inline fun <reified T : Any?> Fragment.viewLifecycleLazy(
    lifecycleState: Lifecycle.State,
    noinline initializer: () -> T
) = ViewLifecycleLazy(viewLifecycleOwnerLiveData, lifecycleState, initializer)
