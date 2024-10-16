/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import org.lineageos.twelve.datasources.MediaDataSource

/**
 * A provider instance. Two instances are the same if they have the same [typeId] and [type].
 * The [type] determines how data should be retrieved from the provider.
 * Each provider has an associated [MediaDataSource] and related arguments, but those are not
 * exposed outside of the media repository.
 *
 * @param type The provider type
 * @param typeId The ID of the provider relative to the [ProviderType]
 * @param name The name of the provider given by the user
 */
class Provider(
    val type: ProviderType,
    val typeId: Long,
    val name: String,
) : UniqueItem<Provider> {
    override fun areItemsTheSame(other: Provider) = compareValuesBy(
        this,
        other,
        Provider::typeId,
        Provider::type,
    ) == 0

    override fun areContentsTheSame(other: Provider) = compareValuesBy(
        this,
        other,
        Provider::name,
    ) == 0
}
