/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.lineageos.twelve.database.converters.UriConverter
import org.lineageos.twelve.database.dao.ItemDao
import org.lineageos.twelve.database.dao.PlaylistDao
import org.lineageos.twelve.database.dao.PlaylistItemCrossRefDao
import org.lineageos.twelve.database.dao.PlaylistWithItemsDao
import org.lineageos.twelve.database.entities.Item
import org.lineageos.twelve.database.entities.Playlist
import org.lineageos.twelve.database.entities.PlaylistItemCrossRef

@Database(
    entities = [
        Playlist::class,
        Item::class,
        PlaylistItemCrossRef::class,
    ],
    version = 1,
)
@TypeConverters(UriConverter::class)
abstract class TwelveDatabase : RoomDatabase() {
    abstract fun getItemDao(): ItemDao
    abstract fun getPlaylistDao(): PlaylistDao
    abstract fun getPlaylistItemCrossRefDao(): PlaylistItemCrossRefDao
    abstract fun getPlaylistWithItemsDao(): PlaylistWithItemsDao

    companion object {
        @Volatile
        private var INSTANCE: TwelveDatabase? = null

        // Singleton to get the instance of the database
        fun getInstance(context: Context): TwelveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TwelveDatabase::class.java,
                    "twelve_database",
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
