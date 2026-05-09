package com.maheswara660.tuneora.core.ui.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.maheswara660.tuneora.core.common.model.Song
import java.io.File

object DeleteUtils {
    fun deleteSong(context: Context, song: Song, onDeleted: () -> Unit) {
        try {
            val file = File(song.path)
            if (file.exists()) {
                if (file.delete()) {
                    // Update MediaStore
                    context.contentResolver.delete(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        "${MediaStore.Audio.Media._ID} = ?",
                        arrayOf(song.id.toString())
                    )
                    onDeleted()
                    Toast.makeText(context, "Deleted ${song.title}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
                }
            } else {
                // If file doesn't exist, just remove from MediaStore
                context.contentResolver.delete(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    "${MediaStore.Audio.Media._ID} = ?",
                    arrayOf(song.id.toString())
                )
                onDeleted()
                Toast.makeText(context, "Removed from library", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
