package com.maheswara660.tuneora.core.data.repository

import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.common.MediaSynchronizer
import com.maheswara660.tuneora.core.data.local.dao.SongDao
import com.maheswara660.tuneora.core.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import android.net.Uri
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MusicRepository @Inject constructor(
    private val songDao: SongDao,
    private val mediaSynchronizer: MediaSynchronizer,
    private val preferencesRepository: PreferencesRepository,
) {
    init {
        mediaSynchronizer.startSync()
    }

    val songs: Flow<List<Song>> = combine(
        songDao.getAllSongs(),
        preferencesRepository.applicationPreferences
    ) { entities, prefs ->
        entities.map { it.toModel() }
            .filter { song ->
                val isShort = if (prefs.filterShortTracks) {
                    song.duration < prefs.minTrackDurationSeconds * 1000
                } else {
                    false
                }
                
                val isBlacklisted = prefs.blacklistedFolders.any { folder ->
                    song.path.startsWith(folder)
                }
                
                !isShort && !isBlacklisted
            }
    }

    fun getAllSongs(): Flow<List<Song>> = songs



    suspend fun insertSongs(songs: List<Song>) {
        songDao.insertSongs(songs.map { it.toEntity() })
    }

    suspend fun deleteAllSongs() {
        songDao.deleteAllSongs()
    }

    suspend fun deleteSongs(ids: List<Long>) {
        songDao.deleteSongs(ids)
    }

    suspend fun deleteSongsPermanently(songs: List<Song>): Boolean {
        val result = mediaSynchronizer.delete(songs)
        if (result) {
            songDao.deleteSongs(songs.map { it.id })
        }
        return result
    }

    fun getHistorySongs(): Flow<List<Song>> = songDao.getHistorySongs().map { entities ->
        entities.map { it.toModel() }
    }

    fun getFavoriteSongs(): Flow<List<Song>> = songDao.getFavoriteSongs().map { entities ->
        entities.map { it.toModel() }
    }
    
    suspend fun getSongById(id: Long): Song? = songDao.getSongById(id)?.toModel()

    suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        songDao.updateFavoriteStatus(songId, isFavorite)
    }

    suspend fun updateLastPlayed(songId: Long) {
        songDao.updateLastPlayed(songId, System.currentTimeMillis())
    }

    suspend fun refreshLibrary() {
        mediaSynchronizer.refresh()
    }
}

private fun SongEntity.toModel(): Song = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    duration = duration,
    uri = Uri.parse(uri),
    albumId = albumId,
    trackNumber = trackNumber,
    year = year,
    path = path,
    dateAdded = dateAdded,
    lastPlayed = lastPlayed,
    isFavorite = isFavorite
)

private fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    duration = duration,
    uri = uri.toString(),
    albumId = albumId,
    trackNumber = trackNumber,
    year = year,
    path = path,
    dateAdded = dateAdded,
    lastPlayed = lastPlayed,
    isFavorite = isFavorite
)

