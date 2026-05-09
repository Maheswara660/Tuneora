package com.maheswara660.tuneora.core.media.audio

import androidx.media3.common.C
import java.nio.ByteBuffer

object TuneoraMediaUtil {
    fun getInt24(buffer: ByteBuffer, position: Int): Int {
        return (buffer.get(position).toInt() and 0xFF) or
               ((buffer.get(position + 1).toInt() and 0xFF) shl 8) or
               ((buffer.get(position + 2).toInt() and 0xFF) shl 16)
    }

    fun putInt24(buffer: ByteBuffer, value: Int) {
        buffer.put((value and 0xFF).toByte())
        buffer.put(((value shr 8) and 0xFF).toByte())
        buffer.put(((value shr 16) and 0xFF).toByte())
    }
    
    fun getBitDepth(encoding: Int): Int {
        return when (encoding) {
            C.ENCODING_PCM_8BIT -> 8
            C.ENCODING_PCM_16BIT -> 16
            C.ENCODING_PCM_24BIT -> 24
            C.ENCODING_PCM_32BIT -> 32
            C.ENCODING_PCM_FLOAT -> 32
            else -> 0
        }
    }
}
