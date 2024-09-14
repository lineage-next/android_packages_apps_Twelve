/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Request status for flows.
 */
sealed class RequestStatus<T : Any> {
    /**
     * Result is not ready yet.
     *
     * @param progress An optional percentage of the request progress
     */
    class Loading<T : Any>(
        @androidx.annotation.IntRange(from = 0, to = 100) val progress: Int? = null
    ) : RequestStatus<T>()

    /**
     * The result is ready.
     *
     * @param data The obtained data
     */
    class Success<T : Any>(val data: T) : RequestStatus<T>()

    /**
     * The request failed.
     *
     * @param type The error type
     */
    class Error<T : Any>(val type: Type) : RequestStatus<T>() {
        enum class Type {
            /**
             * The item was not found.
             */
            NOT_FOUND,

            /**
             * I/O error, can also be network.
             */
            IO,

            /**
             * Authentication error.
             */
            AUTHENTICATION_REQUIRED,

            /**
             * Invalid credentials.
             */
            INVALID_CREDENTIALS,
        }
    }
}
