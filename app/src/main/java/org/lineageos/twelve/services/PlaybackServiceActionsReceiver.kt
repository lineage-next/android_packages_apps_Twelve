/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

/**
 * [BroadcastReceiver] used to handle playback actions from other UI components like widgets.
 */
class PlaybackServiceActionsReceiver : BroadcastReceiver() {
    private val coroutineScope = MainScope()

    private val actionToMethod = mapOf(
        ACTION_TOGGLE_PLAY_PAUSE to ::togglePlayPause
    )

    override fun onReceive(context: Context?, intent: Intent?) {
        coroutineScope.launch {
            actionToMethod[intent?.action]?.invoke(context ?: return@launch)
        }
    }

    private suspend fun togglePlayPause(context: Context) {
        withMediaController(context) { mediaController ->
            if (mediaController.isPlaying) {
                mediaController.pause()
            } else {
                mediaController.play()
            }
        }
    }

    private suspend fun withMediaController(
        context: Context,
        block: suspend (MediaController) -> Unit,
    ) {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        val mediaController = MediaController.Builder(context.applicationContext, sessionToken)
            .buildAsync()
            .await()

        block(mediaController)

        mediaController.release()
    }

    companion object {
        const val ACTION_TOGGLE_PLAY_PAUSE = "TOGGLE_PLAY_PAUSE"
    }
}
