/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.media.audiofx.Visualizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.TwelveApplication
import org.lineageos.twelve.ext.waveFormFlow
import kotlin.math.abs

class VisualizerViewModel(application: Application) : AndroidViewModel(application) {
    private val samplingRate = 1.toMilliHertz()

    private val visualizerFlow = channelFlow {
        val visualizer = Visualizer(getApplication<TwelveApplication>().audioSessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[0]
        }

        trySend(visualizer)

        awaitClose {
            visualizer.release()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val waveform = visualizerFlow.filterNotNull()
        .flowOn(Dispatchers.Default)
        .flatMapLatest { it.waveFormFlow(samplingRate) }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ByteArray(0)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val amplitude = waveform
        .mapLatest { wave ->
            wave.maxOfOrNull { abs(it.toSignedInt()) } ?: 0.0
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0.0
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val energy = waveform
        .mapLatest { wave ->
            wave.map { it.toSignedInt() }.map { it * it }.average().takeIf { !it.isNaN() } ?: 0.0
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0.0
        )

    private fun Byte.toSignedInt() = (toInt() and 0xFF) - 128
    private fun Int.toMilliHertz() = 1000 / this
}
