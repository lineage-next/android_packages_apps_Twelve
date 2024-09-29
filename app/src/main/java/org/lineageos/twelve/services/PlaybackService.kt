/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.coroutineScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import kotlinx.coroutines.guava.future
import org.lineageos.twelve.MainActivity
import org.lineageos.twelve.R
import org.lineageos.twelve.TwelveApplication

@OptIn(UnstableApi::class)
class PlaybackService : MediaLibraryService(), LifecycleOwner {
    private val dispatcher = ServiceLifecycleDispatcher(this)
    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle

    private var mediaLibrarySession: MediaLibrarySession? = null

    private val mediaRepositoryTree by lazy {
        MediaRepositoryTree(
            applicationContext,
            (application as TwelveApplication).mediaRepository,
        )
    }

    private val mediaLibrarySessionCallback = object : MediaLibrarySession.Callback {
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ) = lifecycle.coroutineScope.future {
            MediaSession.MediaItemsWithStartPosition(
                mediaRepositoryTree.getResumptionPlaylist(), 0, 0
            )
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?,
        ) = lifecycle.coroutineScope.future {
            LibraryResult.ofItem(mediaRepositoryTree.getRootMediaItem(), params)
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String,
        ) = lifecycle.coroutineScope.future {
            mediaRepositoryTree.getItem(mediaId)?.let {
                LibraryResult.ofItem(it, null)
            } ?: LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
        }

        @OptIn(UnstableApi::class)
        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ) = lifecycle.coroutineScope.future {
            LibraryResult.ofItemList(mediaRepositoryTree.getChildren(parentId), params)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>,
        ) = lifecycle.coroutineScope.future {
            mediaRepositoryTree.resolveMediaItems(mediaItems)
        }

        @OptIn(UnstableApi::class)
        override fun onSetMediaItems(
            mediaSession: MediaSession,
            browser: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>,
            startIndex: Int,
            startPositionMs: Long,
        ) = lifecycle.coroutineScope.future {
            MediaSession.MediaItemsWithStartPosition(
                mediaRepositoryTree.resolveMediaItems(mediaItems),
                startIndex,
                startPositionMs
            )
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?,
        ) = lifecycle.coroutineScope.future {
            session.notifySearchResultChanged(
                browser, query, mediaRepositoryTree.search(query).size, params
            )
            LibraryResult.ofVoid()
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ) = lifecycle.coroutineScope.future {
            LibraryResult.ofItemList(mediaRepositoryTree.search(query), params)
        }
    }

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setRenderersFactory(TurntableRenderersFactory(this))
            .build()

        mediaLibrarySession = MediaLibrarySession.Builder(
            this, exoPlayer, mediaLibrarySessionCallback
        )
            .setSessionActivity(getSingleTopActivity())
            .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .build()
                .apply {
                    setSmallIcon(R.drawable.ic_notification_small_icon)
                }
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    @Suppress("Deprecation")
    @Deprecated("Deprecated in Java")
    override fun onStart(intent: Intent?, startId: Int) {
        dispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()

        mediaLibrarySession?.player?.release()
        mediaLibrarySession?.release()
        mediaLibrarySession = null

        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaLibrarySession

    private fun getSingleTopActivity() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_OPEN_NOW_PLAYING, true)
        },
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
