package com.maheswara660.tuneora.core.common.model

import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.util.ParsableByteArray
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import java.nio.charset.Charset

@Parcelize
enum class SpeakerEntity(
    val isVoice2: Boolean = false,
    val isGroup: Boolean = false,
    val isBackground: Boolean = false,
    val isWidthLimited: Boolean = false,
) : Parcelable {
    Voice,
    VoiceBackground(isBackground = true),
    Voice1(isWidthLimited = true),
    Voice1Background(isWidthLimited = true, isBackground = true),
    Voice2(isWidthLimited = true, isVoice2 = true),
    Voice2Background(isWidthLimited = true, isVoice2 = true, isBackground = true),
    Group(isGroup = true),
    GroupBackground(isGroup = true, isBackground = true)
}

sealed class SemanticLyrics : Parcelable {
    abstract val unsyncedText: List<Pair<String, SpeakerEntity?>>

    @Parcelize
    data class UnsyncedLyrics(override val unsyncedText: List<Pair<String, SpeakerEntity?>>) :
        SemanticLyrics()

    @Parcelize
    data class SyncedLyrics(val text: List<LyricLine>) : SemanticLyrics() {
        override val unsyncedText
            get() = text.map { it.text to it.speaker }
    }

    @Parcelize
    data class LyricLine(
        val text: String,
        val start: ULong,
        var end: ULong,
        var endIsImplicit: Boolean,
        val words: MutableList<Word>?,
        var speaker: SpeakerEntity?,
        var isTranslated: Boolean
    ) : Parcelable {
        val isClickable: Boolean
            get() = text.isNotBlank()
        val timeRange: ULongRange
            get() = start..end
    }

    @Parcelize
    data class Word(
        var begin: ULong,
        var endInclusive: ULong?,
        var charRange: @kotlinx.parcelize.WriteWith<IntRangeParceler>() IntRange,
        var isRtl: Boolean
    ) : Parcelable {
        constructor(timeRange: ULongRange, charRange: IntRange, isRtl: Boolean)
                : this(timeRange.first, timeRange.last, charRange, isRtl)

        val timeRange
            get() = begin..(endInclusive ?: begin)
    }

    object IntRangeParceler : Parceler<IntRange> {
        override fun create(parcel: Parcel) = parcel.readInt()..parcel.readInt()

        override fun IntRange.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(first)
            parcel.writeInt(last)
        }
    }
}

object UsltFrameDecoder {
    sealed class Result {
        data class Uslt(val language: String, val description: String, val text: String) : Result()
        data class Sylt(
            val language: String,
            val contentType: Int,
            val description: String,
            val lines: List<Line>
        ) : Result() {
            data class Line(val timestamp: UInt, val text: String)
        }
    }

    private const val ID3_TEXT_ENCODING_ISO_8859_1 = 0
    private const val ID3_TEXT_ENCODING_UTF_16 = 1
    private const val ID3_TEXT_ENCODING_UTF_16BE = 2
    private const val ID3_TEXT_ENCODING_UTF_8 = 3

    fun decode(id3Data: ParsableByteArray): Result.Uslt? {
        if (id3Data.limit() < 4) return null
        val encoding = id3Data.readUnsignedByte()
        val charset = getCharset(encoding)
        val lang = ByteArray(3)
        id3Data.readBytes(lang, 0, 3)
        val language = String(lang, Charsets.ISO_8859_1)
        
        val rest = ByteArray(id3Data.bytesLeft())
        id3Data.readBytes(rest, 0, rest.size)

        val descriptionEndIndex = indexOfEos(rest, 0, encoding)
        val description = String(rest, 0, descriptionEndIndex, charset)
        val textStartIndex = descriptionEndIndex + delimiterLength(encoding)
        if (textStartIndex >= rest.size) return Result.Uslt(language, description, "")
        val textEndIndex = indexOfEos(rest, textStartIndex, encoding)
        val text = String(rest, textStartIndex, textEndIndex - textStartIndex, charset)
        return Result.Uslt(language, description, text)
    }

    fun decodeSylt(sampleRate: Int, id3Data: ParsableByteArray): Result.Sylt? {
        // Basic implementation for SYLT decoding
        if (id3Data.limit() < 4) return null
        val encoding = id3Data.readUnsignedByte()
        val charset = getCharset(encoding)
        val lang = ByteArray(3)
        id3Data.readBytes(lang, 0, 3)
        val language = String(lang, Charsets.ISO_8859_1)
        val timeStampFormat = id3Data.readUnsignedByte()
        val contentType = id3Data.readUnsignedByte()
        
        // Skip description
        val description = "" // Simplified
        
        val lines = mutableListOf<Result.Sylt.Line>()
        // This would require full parsing of SYLT chunks
        return Result.Sylt(language, contentType, description, lines)
    }

    private fun getCharset(encoding: Int): Charset {
        return when (encoding) {
            ID3_TEXT_ENCODING_UTF_16 -> Charsets.UTF_16
            ID3_TEXT_ENCODING_UTF_16BE -> Charsets.UTF_16BE
            ID3_TEXT_ENCODING_UTF_8 -> Charsets.UTF_8
            else -> Charsets.ISO_8859_1
        }
    }

    private fun delimiterLength(encoding: Int): Int {
        return if (encoding == ID3_TEXT_ENCODING_ISO_8859_1 || encoding == ID3_TEXT_ENCODING_UTF_8) 1 else 2
    }

    private fun indexOfEos(data: ByteArray, fromIndex: Int, encoding: Int): Int {
        if (encoding == ID3_TEXT_ENCODING_ISO_8859_1 || encoding == ID3_TEXT_ENCODING_UTF_8) {
            for (i in fromIndex until data.size) {
                if (data[i] == 0.toByte()) return i
            }
        } else {
            for (i in fromIndex until data.size - 1 step 2) {
                if (data[i] == 0.toByte() && data[i + 1] == 0.toByte()) return i
            }
        }
        return data.size
    }
}
