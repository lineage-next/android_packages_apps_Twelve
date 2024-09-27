/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import androidx.annotation.OptIn
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer

/**
 * [AudioProcessor] that does nothing other than exposing the [AudioProcessor.AudioFormat].
 * Here the input is the output.
 */
@OptIn(UnstableApi::class)
class ProxyAudioProcessor : AudioProcessor {
    private var pendingAudioFormat = AudioProcessor.AudioFormat.NOT_SET
    private var audioFormat = AudioProcessor.AudioFormat.NOT_SET
        set(value) {
            field = value
            _audioFormatFlow.value = value.takeIf { it != AudioProcessor.AudioFormat.NOT_SET }
        }
    private var buffer = AudioProcessor.EMPTY_BUFFER
    private var isEnded = true

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat) = inputAudioFormat.also {
        this.pendingAudioFormat = it
    }

    override fun isActive() = pendingAudioFormat !== AudioProcessor.AudioFormat.NOT_SET

    override fun queueInput(inputBuffer: ByteBuffer) {
        this.buffer = inputBuffer
    }

    override fun queueEndOfStream() {
        isEnded = true
    }

    override fun getOutput() = buffer.also {
        buffer = AudioProcessor.EMPTY_BUFFER
    }

    override fun isEnded() = isEnded && buffer === AudioProcessor.EMPTY_BUFFER

    override fun flush() {
        buffer = AudioProcessor.EMPTY_BUFFER
        isEnded = false
        audioFormat = pendingAudioFormat
    }

    override fun reset() {
        flush()
        pendingAudioFormat = AudioProcessor.AudioFormat.NOT_SET
        audioFormat = AudioProcessor.AudioFormat.NOT_SET
    }

    companion object {
        private val _audioFormatFlow = MutableStateFlow<AudioProcessor.AudioFormat?>(null)
        val audioFormatFlow = _audioFormatFlow.asStateFlow()
    }
}
