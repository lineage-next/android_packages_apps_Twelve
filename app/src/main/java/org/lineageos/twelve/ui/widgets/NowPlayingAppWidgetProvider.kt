/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.widgets

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.guava.await
import org.lineageos.twelve.MainActivity
import org.lineageos.twelve.R
import org.lineageos.twelve.services.PlaybackService

class NowPlayingAppWidgetProvider : BaseAppWidgetProvider(
    R.layout.app_widget_now_playing
) {
    private val sessionToken by lazy {
        SessionToken(
            context!!,
            ComponentName(context!!, PlaybackService::class.java)
        )
    }

    private val mediaControllerFlow = channelFlow {
        val mediaController = MediaController.Builder(context!!, sessionToken)
            .buildAsync()
            .await()

        trySend(mediaController)

        awaitClose {
            mediaController.release()
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            coroutineScope,
            SharingStarted.Eagerly,
            null
        )

    override fun RemoteViews.update(context: Context) {
        val mediaController = mediaControllerFlow.value ?: return

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_OPEN_NOW_PLAYING, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setOnClickPendingIntent(R.id.linearLayout, pendingIntent)
    }
}
