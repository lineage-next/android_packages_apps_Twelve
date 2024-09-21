/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class PlaylistWithBoolean(
    @Embedded val playlist: Playlist,
    @ColumnInfo(name = "value") val value: Boolean
) {
    fun toPair() = playlist to value
}
