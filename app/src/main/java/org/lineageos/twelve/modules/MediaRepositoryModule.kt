/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.repositories.MediaRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaRepositoryModule {
    @Provides
    @Singleton
    fun provideMediaRepository(
        @ApplicationContext context: Context,
        database: TwelveDatabase
    ) = MediaRepository(context, database)
}
