/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lineageos.twelve.models.ProviderType
import org.lineageos.twelve.models.RequestStatus

@OptIn(ExperimentalCoroutinesApi::class)
class ManageProviderViewModel(application: Application) : ProvidersViewModel(application) {
    /**
     * The provider instance identifiers to manage.
     */
    private val providerInstanceIds = MutableStateFlow<Pair<ProviderType, Long>?>(null)

    /**
     * The user defined provider type. The one in [providerInstanceIds] will always take
     * precedence over this.
     */
    private val _selectedProviderType = MutableStateFlow<ProviderType?>(null)

    /**
     * Whether we're managing an existing provider or adding a new one.
     */
    val inEditMode = providerInstanceIds
        .mapLatest { it != null }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    /**
     * The provider instance to manage.
     */
    val providerInstance = providerInstanceIds.flatMapLatest {
        it?.let { providerInstanceIds ->
            mediaRepository.providerInstance(
                providerInstanceIds.first, providerInstanceIds.second
            ).mapLatest { providerInstance ->
                providerInstance?.let {
                    RequestStatus.Success(it)
                } ?: RequestStatus.Error(RequestStatus.Error.Type.NOT_FOUND)
            }
        } ?: flowOf(RequestStatus.Success(null))
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RequestStatus.Success(null)
        )

    /**
     * The [Bundle] containing the arguments of the provider instance to manage.
     */
    private val providerInstanceArguments = providerInstanceIds
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.providerArguments(it.first, it.second)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    /**
     * The provider type.
     */
    private val providerType = combine(
        _selectedProviderType,
        providerInstance,
    ) { selectedProviderType, providerInstance ->
        when (providerInstance) {
            is RequestStatus.Success -> {
                providerInstance.data?.type
            }

            else -> null
        } ?: selectedProviderType
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    /**
     * The provider type and the arguments of the provider instance to manage.
     */
    val providerTypeWithArguments = combine(
        providerType,
        providerInstanceArguments,
    ) { providerType, providerInstanceArguments ->
        providerType to providerInstanceArguments
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null to null
        )

    /**
     * Set the provider instance to manage. When null, we're adding a new provider.
     */
    fun setProviderInstance(providerInstance: Pair<ProviderType, Long>?) {
        providerInstanceIds.value = providerInstance
    }

    fun setProviderType(providerType: ProviderType?) {
        _selectedProviderType.value = providerType
    }

    /**
     * Add a new provider.
     */
    fun addProvider(
        providerType: ProviderType, name: String, arguments: Bundle
    ) = viewModelScope.launch {
        mediaRepository.addProvider(providerType, name, arguments)
    }

    /**
     * Update the provider.
     */
    fun updateProvider(name: String, arguments: Bundle) = viewModelScope.launch {
        val (providerType, providerTypeId) = providerInstanceIds.value ?: return@launch

        mediaRepository.updateProvider(providerType, providerTypeId, name, arguments)
    }

    /**
     * Delete the provider.
     */
    fun deleteProvider() = viewModelScope.launch {
        val (providerType, providerTypeId) = providerInstanceIds.value ?: return@launch

        mediaRepository.deleteProvider(providerType, providerTypeId)
    }
}
