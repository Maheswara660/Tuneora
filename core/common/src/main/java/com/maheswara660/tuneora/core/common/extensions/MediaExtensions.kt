package com.maheswara660.tuneora.core.common.extensions

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.maheswara660.tuneora.core.common.model.Song

const val EXTRA_SONG_ID = "song_id"
const val EXTRA_ALBUM_ID = "album_id"
const val EXTRA_DATE_ADDED = "date_added"
const val EXTRA_PATH = "path"

fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setDurationMs(duration)
                .setTrackNumber(trackNumber)
                .setReleaseYear(year)
                .setExtras(Bundle().apply {
                    putLong(EXTRA_SONG_ID, id)
                    putLong(EXTRA_ALBUM_ID, albumId)
                    putLong(EXTRA_DATE_ADDED, dateAdded)
                    putString(EXTRA_PATH, path)
                })
                .build()
        )
        .build()
}

fun MediaItem.toSong(): Song {
    val metadata = mediaMetadata
    val extras = metadata.extras ?: Bundle.EMPTY
    return Song(
        id = extras.getLong(EXTRA_SONG_ID, mediaId.toLongOrNull() ?: -1L),
        title = metadata.title?.toString() ?: "",
        artist = metadata.artist?.toString() ?: "",
        album = metadata.albumTitle?.toString() ?: "",
        duration = metadata.durationMs ?: 0L,
        uri = localConfiguration?.uri ?: Uri.EMPTY,
        albumId = extras.getLong(EXTRA_ALBUM_ID, -1L),
        trackNumber = metadata.trackNumber ?: 0,
        year = metadata.releaseYear ?: 0,
        path = extras.getString(EXTRA_PATH, ""),
        dateAdded = extras.getLong(EXTRA_DATE_ADDED, 0L),
        isFavorite = false // This would need to be checked from database
    )
}
