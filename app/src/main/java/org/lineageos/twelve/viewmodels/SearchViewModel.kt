/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest

class SearchViewModel(application: Application) : TwelveViewModel(application) {
    private val searchQuery = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults = searchQuery
        .mapLatest {
            delay(500)
            it
        }
        .flatMapLatest { query ->
            query?.trim()?.takeIf { it.isNotEmpty() }?.let {
                mediaRepository.search("%${it}%")
            } ?: flowOf(null)
        }

    fun setSearchQuery(query: String?) {
        searchQuery.value = query
    }
}
