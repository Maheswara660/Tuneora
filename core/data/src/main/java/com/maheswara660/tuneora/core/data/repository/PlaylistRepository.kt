package com.maheswara660.tuneora.core.data.repository

import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.common.model.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>
}
