package com.maheswara660.tuneora.core.media.audio

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import com.maheswara660.tuneora.core.common.extensions.EXTRA_ALBUM_ID
import com.maheswara660.tuneora.core.common.extensions.EXTRA_DATE_ADDED
import com.maheswara660.tuneora.core.common.extensions.EXTRA_PATH
import com.maheswara660.tuneora.core.common.extensions.EXTRA_SONG_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

class LastPlayedManager(
    context: Context,
    private val controller: EndedWorkaroundPlayer
) {

    companion object {
        private const val TAG = "LastPlayedManager"
    }

    var allowSavingState = true
    private var job: Job? = null
    private val prefs by lazy { context.getSharedPreferences("LastPlayedManager", 0) }

    private fun dumpPlaylist(): MediaItemsWithStartPosition {
        val items = mutableListOf<MediaItem>()
        for (i in 0 until controller.mediaItemCount) {
            items.add(controller.getMediaItemAt(i))
        }
        return MediaItemsWithStartPosition(
            items, controller.currentMediaItemIndex, controller.currentPosition
        )
    }

    fun eraseShuffleOrder() {
        prefs.edit(commit = true) {
            remove("shuffle_persist")
        }
    }

    fun save() {
        if (!allowSavingState) {
            return
        }
        val data = dumpPlaylist()
        val repeatMode = controller.repeatMode
        val shuffleModeEnabled = controller.shuffleModeEnabled
        val playbackParameters = controller.playbackParameters
        val persistent = if (controller.shuffleModeEnabled)
            CircularShuffleOrder.Persistent(controller.shuffleOrder as CircularShuffleOrder)
        else null
        val ended = controller.playbackState == Player.STATE_ENDED
        
        job?.cancel()
        job = CoroutineScope(Dispatchers.Default).launch {
            val lastPlayed = PrefsListUtils.dump(
                data.mediaItems.map {
                    val b = SafeDelimitedStringConcat(":")
                    b.writeStringUnsafe("ver_1")
                    b.writeStringSafe(it.mediaId)
                    b.writeUri(it.localConfiguration?.uri)
                    b.writeStringSafe(it.localConfiguration?.mimeType)
                    b.writeStringSafe(it.mediaMetadata.title)
                    b.writeStringSafe(it.mediaMetadata.artist)
                    b.writeStringSafe(it.mediaMetadata.albumTitle)
                    b.writeStringSafe(it.mediaMetadata.albumArtist)
                    b.writeUri(it.mediaMetadata.artworkUri)
                    b.writeInt(it.mediaMetadata.trackNumber)
                    b.writeInt(it.mediaMetadata.discNumber)
                    b.writeInt(it.mediaMetadata.recordingYear)
                    b.writeInt(it.mediaMetadata.releaseYear)
                    b.writeBool(it.mediaMetadata.isBrowsable)
                    b.writeBool(it.mediaMetadata.isPlayable)
                    
                    val extras = it.mediaMetadata.extras ?: Bundle.EMPTY
                    b.writeLong(extras.getLong(EXTRA_DATE_ADDED))
                    b.writeLong(extras.getLong(EXTRA_ALBUM_ID))
                    b.writeLong(extras.getLong(EXTRA_SONG_ID))
                    b.writeStringSafe(extras.getString(EXTRA_PATH))
                    
                    b.writeLong(it.mediaMetadata.durationMs)
                    b.toString()
                })
            prefs.edit {
                putStringSet("last_played_lst", lastPlayed.first)
                putString("last_played_grp", lastPlayed.second)
                putInt("last_played_idx", data.startIndex)
                putLong("last_played_pos", data.startPositionMs)
                putInt("repeat_mode", repeatMode)
                putBoolean("shuffle", shuffleModeEnabled)
                putString("shuffle_persist", persistent?.toString())
                putBoolean("ended", ended)
                putFloat("speed", playbackParameters.speed)
                putFloat("pitch", playbackParameters.pitch)
                apply()
            }
        }
    }

    suspend fun restore(callback: (MediaItemsWithStartPosition?, CircularShuffleOrder.Persistent) -> Unit) {
        withContext(Dispatchers.Default) {
            val seed = try {
                CircularShuffleOrder.Persistent.deserialize(
                    prefs.getString("shuffle_persist", null)
                )
            } catch (e: Exception) {
                eraseShuffleOrder()
                throw e
            }
            try {
                val lastPlayedLst = prefs.getStringSet("last_played_lst", null)?.toSet()
                val lastPlayedGrp = prefs.getString("last_played_grp", null)
                val lastPlayedIdx = prefs.getInt("last_played_idx", 0)
                val lastPlayedPos = prefs.getLong("last_played_pos", 0)
                if (lastPlayedGrp == null || lastPlayedLst == null) {
                    runCallback(callback, seed) { null }
                    return@withContext
                }
                val repeatMode = prefs.getInt("repeat_mode", Player.REPEAT_MODE_OFF)
                val shuffleModeEnabled = prefs.getBoolean("shuffle", false)
                val ended = prefs.getBoolean("ended", false)
                val playbackParameters = PlaybackParameters(
                    prefs.getFloat("speed", 1f),
                    prefs.getFloat("pitch", 1f)
                )
                val mediaItems = PrefsListUtils.parse(lastPlayedLst, lastPlayedGrp)
                    .map {
                        val b = SafeDelimitedStringDecat(":", it)
                        val versionStr = b.readStringUnsafe()
                        val mediaId = b.readStringSafe() ?: ""
                        val uri = b.readUri()
                        val mimeType = b.readStringSafe()
                        val title = b.readStringSafe()
                        val artist = b.readStringSafe()
                        val album = b.readStringSafe()
                        val albumArtist = b.readStringSafe()
                        val imgUri = b.readUri()
                        val trackNumber = b.readInt()
                        val discNumber = b.readInt()
                        val recordingYear = b.readInt()
                        val releaseYear = b.readInt()
                        val isBrowsable = b.readBool()
                        val isPlayable = b.readBool()
                        
                        val dateAdded = b.readLong()
                        val albumId = b.readLong()
                        val songId = b.readLong()
                        val path = b.readStringSafe()
                        val duration = b.readLong()
                        
                        MediaItem.Builder()
                            .setUri(uri)
                            .setMediaId(mediaId)
                            .setMimeType(mimeType)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(title)
                                    .setArtist(artist)
                                    .setAlbumTitle(album)
                                    .setAlbumArtist(albumArtist)
                                    .setArtworkUri(imgUri)
                                    .setTrackNumber(trackNumber)
                                    .setDiscNumber(discNumber)
                                    .setRecordingYear(recordingYear)
                                    .setReleaseYear(releaseYear)
                                    .setDurationMs(duration)
                                    .setIsBrowsable(isBrowsable ?: false)
                                    .setIsPlayable(isPlayable ?: true)
                                    .setExtras(Bundle().apply {
                                        if (dateAdded != null) putLong(EXTRA_DATE_ADDED, dateAdded)
                                        if (albumId != null) putLong(EXTRA_ALBUM_ID, albumId)
                                        if (songId != null) putLong(EXTRA_SONG_ID, songId)
                                        if (path != null) putString(EXTRA_PATH, path)
                                    })
                                    .build()
                            )
                            .build()
                    }
                
                val data = MediaItemsWithStartPosition(mediaItems, lastPlayedIdx, lastPlayedPos)
                runCallback(callback, seed) {
                    controller.isEnded = ended
                    controller.repeatMode = repeatMode
                    controller.shuffleModeEnabled = shuffleModeEnabled
                    controller.playbackParameters = playbackParameters
                    data
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore state", e)
                runCallback(callback, seed) { null }
            }
        }
    }
}

private suspend inline fun runCallback(
    crossinline callback: (
        MediaItemsWithStartPosition?,
        CircularShuffleOrder.Persistent
    ) -> Unit,
    seed: CircularShuffleOrder.Persistent,
    noinline parameter: () -> MediaItemsWithStartPosition?
) {
    withContext(Dispatchers.Main) { callback(parameter(), seed) }
}

private class SafeDelimitedStringConcat(private val delimiter: String) {
    private val b = StringBuilder()
    private var hadFirst = false

    private fun append(s: String?) {
        if (s?.contains(delimiter, false) == true) {
            throw IllegalArgumentException("argument must not contain delimiter")
        }
        if (hadFirst) {
            b.append(delimiter)
        } else {
            hadFirst = true
        }
        s?.let { b.append(it) }
    }

    override fun toString(): String = b.toString()

    fun writeStringUnsafe(s: CharSequence?) = append(s?.toString())
    fun writeBase64(b: ByteArray?) = append(b?.let { Base64.encodeToString(it, Base64.NO_WRAP) })
    fun writeStringSafe(s: CharSequence?) = writeBase64(s?.toString()?.toByteArray(StandardCharsets.UTF_8))
    fun writeInt(i: Int?) = append(i?.toString())
    fun writeLong(i: Long?) = append(i?.toString())
    fun writeBool(b: Boolean?) = append(b?.toString())
    fun writeUri(u: Uri?) = writeStringSafe(u?.toString())
}

private class SafeDelimitedStringDecat(delimiter: String, str: String) {
    private val items = str.split(delimiter)
    private var pos = 0

    private fun read(): String? {
        if (pos >= items.size) return null
        return items[pos++].ifEmpty { null }
    }

    fun readStringUnsafe(): String? = read()
    fun readBase64(): ByteArray? = read()?.let { Base64.decode(it, Base64.NO_WRAP) }
    fun readStringSafe(): String? = readBase64()?.toString(StandardCharsets.UTF_8)
    fun readInt(): Int? = read()?.toIntOrNull()
    fun readLong(): Long? = read()?.toLongOrNull()
    fun readBool(): Boolean? = read()?.toBoolean()
    fun readUri(): Uri? = readStringSafe()?.toUri()
}

private object PrefsListUtils {
    fun parse(stringSet: Set<String>, groupStr: String): List<String> {
        if (groupStr.isEmpty()) return emptyList()
        val groups = groupStr.split(",")
        return groups.map { hc ->
            stringSet.firstOrNull { it.hashCode().toString() == hc }
                ?: throw NoSuchElementException("Could not find hashcode $hc in set")
        }
    }

    fun dump(inList: List<String>): Pair<Set<String>, String> {
        val list = inList.map { it.trim() }
        return Pair(list.toSet(), list.joinToString(",") { it.hashCode().toString() })
    }
}
