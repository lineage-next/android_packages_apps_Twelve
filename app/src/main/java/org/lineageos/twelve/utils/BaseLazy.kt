/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

/**
 * [Lazy] implementation.
 */
abstract class BaseLazy<T : Any?>(
    protected val initializer: () -> T,
) : Lazy<T> {
    @Volatile protected var cachedValue: Any? = UNINITIALIZED_VALUE

    /**
     * Called only when a new value is initialized.
     */
    protected open fun onValueInitialized(value: T) {}

    override val value: T
        get() {
            val v1 = cachedValue
            if (v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST")
                return v1 as T
            }

            return synchronized(this) {
                val v2 = cachedValue
                if (v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (v2 as T)
                } else {
                    val typedValue = initializer()
                    cachedValue = typedValue
                    onValueInitialized(typedValue)
                    typedValue
                }
            }
        }

    override fun isInitialized() = cachedValue !== UNINITIALIZED_VALUE

    companion object {
        @JvmStatic
        protected val UNINITIALIZED_VALUE = Any()
    }
}
