package com.maheswara660.tuneora.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val albumId: Long,
    val trackNumber: Int,
    val year: Int,
    val path: String,
    val dateAdded: Long = 0,
    val lastPlayed: Long = 0,
    val isFavorite: Boolean = false
)

fun SongEntity.toSong() = com.maheswara660.tuneora.core.common.model.Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    duration = duration,
    uri = android.net.Uri.parse(uri),
    albumId = albumId,
    trackNumber = trackNumber,
    year = year,
    path = path,
    dateAdded = dateAdded,
    lastPlayed = lastPlayed,
    isFavorite = isFavorite
)
