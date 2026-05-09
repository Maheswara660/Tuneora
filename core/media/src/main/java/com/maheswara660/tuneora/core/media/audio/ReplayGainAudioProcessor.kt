package com.maheswara660.tuneora.core.media.audio

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.Log
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

class ReplayGainAudioProcessor : BaseAudioProcessor() {
    companion object {
        private const val TAG = "ReplayGainAP"
    }

    // Stub for now, as native lib hificore is not available
    private var compressor: Any? = null 
    var mode = ReplayGainUtil.Mode.None
        private set
    var rgGain = 0 // dB
        private set
    var nonRgGain = 0 // dB
        private set
    var boostGain = 0 // dB
        private set
    var offloadEnabled = false
        private set
    var reduceGain = false
        private set
    var settingsChangedListener: (() -> Unit)? = null
    var boostGainChangedListener: (() -> Unit)? = null
    var offloadEnabledChangedListener: (() -> Unit)? = null

    private var gain = 1f
    private var kneeThresholdDb: Float? = null
    private var outputFloat: Boolean? = null
    private var pendingOutputFloat: Boolean? = null
    private var tags: ReplayGainUtil.ReplayGainInfo? = null

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!isActive) return
        val frameCount = inputBuffer.remaining() / inputAudioFormat.bytesPerFrame
        val outputBuffer = replaceOutputBuffer(frameCount * outputAudioFormat.bytesPerFrame)
        if (inputBuffer.hasRemaining()) {
            if (gain == 1f) {
                outputBuffer.put(inputBuffer)
            } else {
                while (inputBuffer.hasRemaining()) {
                    when (inputAudioFormat.encoding) {
                        C.ENCODING_PCM_8BIT -> outputBuffer.put(
                            (inputBuffer.get() * gain).toInt().toByte()
                        )

                        C.ENCODING_PCM_16BIT, C.ENCODING_PCM_16BIT_BIG_ENDIAN ->
                            outputBuffer.putShort(
                                (inputBuffer.getShort() * gain).toInt().toShort()
                            )

                        C.ENCODING_PCM_24BIT, C.ENCODING_PCM_24BIT_BIG_ENDIAN -> {
                            TuneoraMediaUtil.putInt24(
                                outputBuffer, (TuneoraMediaUtil.getInt24(
                                    inputBuffer,
                                    inputBuffer.position()
                                ) * gain).toInt()
                                    .shl(8).shr(8)
                            )
                            inputBuffer.position(inputBuffer.position() + 3)
                        }

                        C.ENCODING_PCM_32BIT, C.ENCODING_PCM_32BIT_BIG_ENDIAN ->
                            outputBuffer.putInt((inputBuffer.getInt() * gain).toInt())

                        C.ENCODING_PCM_FLOAT -> outputBuffer.putFloat(
                            inputBuffer.getFloat() * gain
                        )

                        else -> {
                            // Fallback for unknown encoding
                            outputBuffer.put(inputBuffer)
                        }
                    }
                }
            }
        }
        outputBuffer.flip()
    }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (TuneoraMediaUtil.getBitDepth(inputAudioFormat.encoding) % 8 != 0)
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        
        pendingOutputFloat = false
        return inputAudioFormat
    }

    fun setMode(mode: ReplayGainUtil.Mode, doNotNotifyListener: Boolean): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.mode == mode) {
                return true
            }
            listener = settingsChangedListener
            this.mode = mode
        }
        if (!doNotNotifyListener) {
            listener?.invoke()
            return applyGain()
        } else return true
    }

    fun setRgGain(rgGain: Int): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.rgGain == rgGain) {
                return true
            }
            listener = settingsChangedListener
            this.rgGain = rgGain
        }
        listener?.invoke()
        return applyGain()
    }

    fun setNonRgGain(nonRgGain: Int): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.nonRgGain == nonRgGain) {
                return true
            }
            listener = settingsChangedListener
            this.nonRgGain = nonRgGain
        }
        listener?.invoke()
        return applyGain()
    }

    fun setBoostGain(boostGain: Int): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.boostGain == boostGain) {
                return true
            }
            listener = boostGainChangedListener
            this.boostGain = boostGain
        }
        listener?.invoke()
        return applyGain()
    }

    fun setReduceGain(reduceGain: Boolean): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.reduceGain == reduceGain) {
                return true
            }
            listener = settingsChangedListener
            this.reduceGain = reduceGain
        }
        listener?.invoke()
        return applyGain()
    }

    fun setOffloadEnabled(offloadEnabled: Boolean): Boolean {
        val listener: (() -> Unit)?
        synchronized(this) {
            if (this.offloadEnabled == offloadEnabled) {
                return true
            }
            listener = offloadEnabledChangedListener
            this.offloadEnabled = offloadEnabled
        }
        listener?.invoke()
        return applyGain()
    }

    fun setRootFormat(inputFormat: Format) {
        tags = ReplayGainUtil.parse(inputFormat)
    }

    private fun computeGain(): Pair<Float, Float?>? {
        val mode: ReplayGainUtil.Mode
        val rgGain: Int
        val reduceGain: Boolean
        synchronized(this) {
            mode = this.mode
            rgGain = this.rgGain
            reduceGain = this.reduceGain
        }
        return ReplayGainUtil.calculateGain(
            tags, mode, rgGain,
            reduceGain, ReplayGainUtil.RATIO
        )
    }

    private fun applyGain(): Boolean {
        val nonRgGain: Int
        synchronized(this) {
            nonRgGain = this.nonRgGain
        }
        val gainResult = computeGain()
        if ((gainResult?.second != null) != outputFloat && outputFloat != null) {
            return false
        }
        this.gain = gainResult?.first ?: ReplayGainUtil.dbToAmpl(nonRgGain.toFloat())
        this.kneeThresholdDb = gainResult?.second
        return true
    }

    override fun onFlush() {
        outputFloat = pendingOutputFloat
        if (!applyGain())
            Log.d(TAG, "raced between flush and configure, do nothing for now")
    }

    override fun onReset() {
        compressor = null
    }
}
