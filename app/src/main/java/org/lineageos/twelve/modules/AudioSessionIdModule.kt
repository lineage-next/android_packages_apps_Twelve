/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.modules

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioSessionIdModule {
    data class AudioSessionId(val id: Int)

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideAudioSessionId(
        @ApplicationContext context: Context
    ) = AudioSessionId(Util.generateAudioSessionIdV21(context))
}
