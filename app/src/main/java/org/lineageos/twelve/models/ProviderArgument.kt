/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.os.Bundle
import androidx.annotation.StringRes
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Argument of a provider.
 *
 * @param T The type of the argument
 * @param key The key of the argument
 * @param type The [KClass] of the argument
 * @param nameStringResId The string resource ID of the argument name
 * @param required Whether this argument is required
 * @param hidden Whether the value of this argument should be hidden
 * @param defaultValue The default value of the argument
 */
data class ProviderArgument<T : Any>(
    val key: String,
    val type: KClass<T>,
    @StringRes val nameStringResId: Int,
    val required: Boolean,
    val hidden: Boolean,
    val defaultValue: T? = null,
) {
    fun getValue(value: T?) = value ?: defaultValue

    companion object {
        /**
         * Get the optional argument from a [Bundle].
         */
        fun <T : Any> Bundle.getArgument(
            providerArguments: ProviderArgument<T>
        ) = when (providerArguments.type) {
            String::class -> getString(providerArguments.key)
            Boolean::class -> getBoolean(providerArguments.key)
            else -> throw Exception("Unsupported type")
        }?.let {
            providerArguments.getValue(providerArguments.type.cast(it))
        }

        /**
         * Get the required argument from a [Bundle].
         */
        fun <T : Any> Bundle.requireArgument(
            providerArguments: ProviderArgument<T>
        ) = getArgument(providerArguments) ?: throw Exception("Argument not found")
    }
}
