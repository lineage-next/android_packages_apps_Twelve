/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.modules

import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MediaControllerModule {
    @Provides
    fun provideMediaController(
        @ApplicationContext context: Context,
        sessionToken: SessionToken,
    ) = MediaController.Builder(context, sessionToken).buildAsync()
}
