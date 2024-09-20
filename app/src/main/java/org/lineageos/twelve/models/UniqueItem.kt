/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * An item that can be uniquely identified.
 */
sealed interface UniqueItem {
    /**
     * Return whether this item is the same as the other.
     */
    fun areItemsTheSame(other: UniqueItem): Boolean

    /**
     * Return whether this item has the same content as the other.
     * This is called only when [areItemsTheSame] returns true.
     */
    fun areContentsTheSame(other: UniqueItem): Boolean

    companion object {
        /**
         * @see areItemsTheSame
         */
        inline fun <reified T : UniqueItem> areItemsTheSame(
            clazz: KClass<T>,
            other: Any?,
            predicate: (T) -> Boolean,
        ): Boolean = clazz.safeCast(other)?.let { predicate(it) } ?: false

        /**
         * @see areContentsTheSame
         */
        inline fun <reified T : UniqueItem> areContentsTheSame(
            clazz: KClass<T>,
            other: Any?,
            predicate: (T) -> Boolean,
        ): Boolean = clazz.safeCast(other)?.let { predicate(it) } ?: false
    }
}
