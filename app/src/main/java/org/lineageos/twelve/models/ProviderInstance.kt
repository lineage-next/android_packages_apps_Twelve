/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * A provider instance. Two instances are the same if they have the same [typeId] and [type].
 *
 * @param typeId The ID of the provider relative to the [ProviderType]
 * @param name A name given by the user
 * @param type The provider type
 */
class ProviderInstance(
    val typeId: Long,
    val name: String,
    val type: ProviderType,
) : UniqueItem<ProviderInstance> {
    override fun areItemsTheSame(other: ProviderInstance) = compareValuesBy(
        this,
        other,
        ProviderInstance::typeId,
        ProviderInstance::type,
    ) == 0

    override fun areContentsTheSame(other: ProviderInstance) = compareValuesBy(
        this,
        other,
        ProviderInstance::name,
    ) == 0
}
