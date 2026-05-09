package com.maheswara660.tuneora.core.common.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri,
    val albumId: Long,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val path: String = "",
    val dateAdded: Long = 0,
    val lastPlayed: Long = 0,
    val isFavorite: Boolean = false
) {
    val artworkUri: Uri get() = Uri.parse("content://media/external/audio/albumart/$albumId")
}

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val year: Int = 0
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int
)

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long = 0
)
