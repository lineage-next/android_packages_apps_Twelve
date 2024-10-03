/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.media.audiofx.Visualizer
import kotlinx.coroutines.channels.awaitClose

fun Visualizer.waveFormFlow(samplingRate: Int) = conflatedCallbackFlow {
    val emptyByteArray = ByteArray(0)

    val listener = object : Visualizer.OnDataCaptureListener {
        override fun onWaveFormDataCapture(
            visualizer: Visualizer?,
            waveform: ByteArray?,
            samplingRate: Int
        ) {
            trySend(waveform?.clone() ?: emptyByteArray)
        }

        override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
            // Ignored
        }
    }

    enabled = false
    setDataCaptureListener(listener, samplingRate, true, false)
    enabled = true

    awaitClose {
        enabled = false
        setDataCaptureListener(null, 0, false, false)
    }
}
