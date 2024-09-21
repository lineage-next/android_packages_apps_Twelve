/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["audio_uri"], unique = true),
    ],
)
data class Item(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "item_id") val id: Long,
    @ColumnInfo(name = "audio_uri") val audioUri: Uri,
    @ColumnInfo(name = "count", defaultValue = "0") val count: Long = 0,
)
