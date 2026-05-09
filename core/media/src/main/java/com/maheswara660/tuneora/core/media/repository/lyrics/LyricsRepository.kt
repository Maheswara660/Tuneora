package com.maheswara660.tuneora.core.media.repository.lyrics

import android.content.Context
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Log
import com.maheswara660.tuneora.core.media.audio.LrcUtils
import com.maheswara660.tuneora.core.common.model.SemanticLyrics
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getLyrics(mediaItem: MediaItem, format: Format?): SemanticLyrics? {
        val options = LrcUtils.LrcParserOptions(
            trim = true,
            multiLine = false,
            errorText = "Failed to parse lyrics"
        )

        val file = getFileFromMediaItem(mediaItem)
        var lrc = LrcUtils.loadAndParseLyricsFile(file, format?.sampleMimeType, options)
        
        if (lrc == null && format != null) {
            val trackMetadata = format.metadata
            if (trackMetadata != null) {
                val extracted = LrcUtils.extractAndParseLyrics(
                    if (format.sampleRate != Format.NO_VALUE) format.sampleRate else 0,
                    format.sampleMimeType,
                    trackMetadata,
                    options
                )
                if (extracted.isNotEmpty()) {
                    lrc = extracted[0]
                }
            }
        }
        
        return lrc
    }

    private fun getFileFromMediaItem(mediaItem: MediaItem): File? {
        return try {
            val uri = mediaItem.localConfiguration?.uri
            if (uri != null && uri.scheme == "file") {
                File(URI.create(uri.toString()))
            } else if (uri != null && uri.scheme == "content") {
                // For content URIs, we might need a different way to get the file path
                // But often local files are passed as file URIs in these types of apps
                null
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LyricsRepository", "Failed to get file from MediaItem", e)
            null
        }
    }
}
