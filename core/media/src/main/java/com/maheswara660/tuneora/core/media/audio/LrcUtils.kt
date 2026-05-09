package com.maheswara660.tuneora.core.media.audio

import androidx.media3.common.Metadata
import androidx.media3.common.util.Log
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.extractor.metadata.id3.BinaryFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import com.maheswara660.tuneora.core.common.model.SemanticLyrics
import com.maheswara660.tuneora.core.common.model.UsltFrameDecoder
import java.io.File
import java.nio.charset.Charset

object LrcUtils {

    private const val TAG = "LrcUtils"

    enum class LyricFormat {
        LRC,
        TTML,
        SRT
    }

    data class LrcParserOptions(val trim: Boolean, val multiLine: Boolean, val errorText: String?)

    fun parseLyrics(
        lyrics: String,
        audioMimeType: String?,
        parserOptions: LrcParserOptions,
        format: LyricFormat?
    ): SemanticLyrics? {
        val parsers = listOf(
            { if (format == null || format == LyricFormat.TTML) parseTtml(audioMimeType, lyrics) else null },
            { if (format == null || format == LyricFormat.SRT) parseSrt(lyrics, parserOptions.trim) else null },
            { if (format == null || format == LyricFormat.LRC) parseLrc(lyrics, parserOptions.trim, parserOptions.multiLine) else null }
        )

        for (parser in parsers) {
            try {
                val result = parser()
                if (result != null) return result
            } catch (e: Exception) {
                if (parserOptions.errorText == null) throw e
                Log.e(TAG, "Failed to parse lyrics", e)
                return SemanticLyrics.UnsyncedLyrics(listOf(("${parserOptions.errorText}\n\n${Log.getThrowableString(e)}\n\n$lyrics") to null))
            }
        }
        return null
    }

    fun extractAndParseLyrics(
        sampleRate: Int,
        audioMimeType: String?,
        metadata: Metadata,
        parserOptions: LrcParserOptions
    ): List<SemanticLyrics> {
        val out = mutableListOf<SemanticLyrics>()
        for (i in 0 until metadata.length()) {
            val meta = metadata.get(i)
            if (meta is BinaryFrame && (meta.id == "SYLT" || meta.id == "SLT")) {
                val syltData = UsltFrameDecoder.decodeSylt(sampleRate, ParsableByteArray(meta.data))
                if (syltData != null) {
                    if (syltData.contentType == 1 || syltData.contentType == 2) {
                        out.add(syltData.toSyncedLyrics(parserOptions.trim))
                    }
                    continue
                }
            }
            val plainTextData = when {
                meta is VorbisComment && meta.key == "LYRICS" -> meta.value
                meta is BinaryFrame && (meta.id == "USLT" || meta.id == "ULT" || meta.id == "SLT" || meta.id == "SYLT") -> 
                    UsltFrameDecoder.decode(ParsableByteArray(meta.data))?.text
                meta is TextInformationFrame && meta.id == "USLT" -> meta.values.joinToString("\n")
                else -> null
            }
            if (plainTextData != null) {
                parseLyrics(plainTextData, audioMimeType, parserOptions, null)?.let {
                    out.add(it)
                }
            }
        }
        out.sortBy {
            if (it !is SemanticLyrics.SyncedLyrics) -10
            else {
                val hasWords = it.text.any { line -> line.words != null }
                val hasTl = it.text.any { line -> line.isTranslated }
                (if (hasWords) 10 else 0) + (if (hasTl) 1 else 0)
            }
        }
        return out
    }

    fun loadAndParseLyricsFile(
        musicFile: File?,
        audioMimeType: String?,
        parserOptions: LrcParserOptions
    ): SemanticLyrics? {
        if (musicFile == null) return null
        val parent = musicFile.parentFile ?: return null
        val name = musicFile.nameWithoutExtension

        return loadTextFile(File(parent, "$name.ttml"), parserOptions.errorText)?.let { parseLyrics(it, audioMimeType, parserOptions, LyricFormat.TTML) }
            ?: loadTextFile(File(parent, "$name.srt"), parserOptions.errorText)?.let { parseLyrics(it, audioMimeType, parserOptions, LyricFormat.SRT) }
            ?: loadTextFile(File(parent, "$name.lrc"), parserOptions.errorText)?.let { parseLyrics(it, audioMimeType, parserOptions, LyricFormat.LRC) }
    }

    private fun loadTextFile(lrcFile: File?, errorText: String?): String? {
        return try {
            if (lrcFile?.exists() == true) lrcFile.readBytes().toString(Charset.defaultCharset())
            else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load lyrics file", e)
            errorText
        }
    }
}
