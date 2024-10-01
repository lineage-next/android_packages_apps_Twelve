/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.modules

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.SessionToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.lineageos.twelve.services.PlaybackService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionTokenModule {
    @Provides
    @Singleton
    fun provideSessionToken(@ApplicationContext context: Context) =
        SessionToken(context, ComponentName(context, PlaybackService::class.java))
}
