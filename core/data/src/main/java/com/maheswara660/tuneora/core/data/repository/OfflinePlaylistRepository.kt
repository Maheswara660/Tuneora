package com.maheswara660.tuneora.core.data.repository

import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.common.model.Playlist
import com.maheswara660.tuneora.core.data.local.dao.PlaylistDao
import com.maheswara660.tuneora.core.data.local.entity.PlaylistEntity
import com.maheswara660.tuneora.core.data.local.entity.PlaylistSongEntity
import com.maheswara660.tuneora.core.data.local.entity.toSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflinePlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {
    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toPlaylist() }
        }
    }

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist.toEntity())
    }

    override suspend fun renamePlaylist(playlistId: Long, newName: String) {
        playlistDao.updatePlaylistName(playlistId, newName)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongEntity(playlistId, songId))
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(PlaylistSongEntity(playlistId, songId))
    }

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.toSong() }
        }
    }
}

private fun PlaylistEntity.toPlaylist() = Playlist(
    id = id,
    name = name,
    createdAt = createdAt
)

private fun Playlist.toEntity() = PlaylistEntity(
    id = id,
    name = name,
    createdAt = createdAt
)
