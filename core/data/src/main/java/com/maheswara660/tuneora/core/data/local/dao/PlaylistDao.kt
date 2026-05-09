package com.maheswara660.tuneora.core.data.local.dao

import androidx.room.*
import com.maheswara660.tuneora.core.data.local.entity.PlaylistEntity
import com.maheswara660.tuneora.core.data.local.entity.PlaylistSongEntity
import com.maheswara660.tuneora.core.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun updatePlaylistName(playlistId: Long, name: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(playlistSong: PlaylistSongEntity)

    @Delete
    suspend fun removeSongFromPlaylist(playlistSong: PlaylistSongEntity)

    @Query("""
        SELECT songs.* FROM songs 
        INNER JOIN playlist_songs ON songs.id = playlist_songs.songId 
        WHERE playlist_songs.playlistId = :playlistId 
        ORDER BY playlist_songs.addedAt DESC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>>
}
