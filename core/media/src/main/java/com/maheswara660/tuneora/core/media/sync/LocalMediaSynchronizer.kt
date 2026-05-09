package com.maheswara660.tuneora.core.media.sync

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.maheswara660.tuneora.core.common.ApplicationScope
import com.maheswara660.tuneora.core.common.Dispatcher
import com.maheswara660.tuneora.core.common.TuneoraDispatchers
import com.maheswara660.tuneora.core.common.extensions.getStorageVolumes
import com.maheswara660.tuneora.core.common.extensions.prettyName
import com.maheswara660.tuneora.core.common.extensions.scanPaths
import com.maheswara660.tuneora.core.common.extensions.scanStorage
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.data.local.dao.DirectoryDao
import com.maheswara660.tuneora.core.data.local.dao.SongDao
import com.maheswara660.tuneora.core.data.local.entity.DirectoryEntity
import com.maheswara660.tuneora.core.data.local.entity.SongEntity
import com.maheswara660.tuneora.core.common.MediaSynchronizer
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

class LocalMediaSynchronizer @Inject constructor(
    private val songDao: SongDao,
    private val directoryDao: DirectoryDao,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @ApplicationContext private val context: Context,
    @Dispatcher(TuneoraDispatchers.IO) private val dispatcher: CoroutineDispatcher,
) : MediaSynchronizer {

    private var mediaSyncingJob: Job? = null

    override suspend fun refresh(path: String?): Boolean {
        return path?.let { context.scanPaths(listOf(path)) }
            ?: context.getStorageVolumes().all { context.scanStorage(it.path) }
    }

    override fun startSync() {
        if (mediaSyncingJob != null) return
        mediaSyncingJob = getMediaSongsFlow().onEach { songs ->
            applicationScope.launch { updateDirectories(songs) }
            applicationScope.launch { updateSongs(songs) }
        }.launchIn(applicationScope)
    }

    override fun stopSync() {
        mediaSyncingJob?.cancel()
        mediaSyncingJob = null
    }

    override suspend fun delete(songs: List<Song>): Boolean = withContext(dispatcher) {
        var allDeleted = true
        val preferences = preferencesRepository.applicationPreferences.first()
        val hasManageExternalStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // Legacy storage model or provided permission
        }

        val useFastDelete = preferences.fastDelete && hasManageExternalStorage

        songs.forEach { song ->
            try {
                if (useFastDelete) {
                    val file = File(song.path)
                    if (file.exists()) {
                        if (file.delete()) {
                            // Also remove from MediaStore to avoid dangling entries
                            context.contentResolver.delete(song.uri, null, null)
                        } else {
                            allDeleted = false
                        }
                    }
                } else {
                    val deletedRows = context.contentResolver.delete(song.uri, null, null)
                    if (deletedRows <= 0) {
                        // Try direct file deletion if MediaStore failed (may still fail due to Scoped Storage)
                        val file = File(song.path)
                        if (file.exists()) {
                            allDeleted = file.delete() && allDeleted
                        }
                    }
                }
            } catch (e: Exception) {
                allDeleted = false
            }
        }
        allDeleted
    }

    private suspend fun updateDirectories(songs: List<Song>) =
        withContext(Dispatchers.Default) {
            val directories = context.getStorageVolumes().flatMap {
                getDirectoryEntities(currentFolder = it, songs = songs)
            }
            directoryDao.upsertAll(directories)

            val currentDirectoryPaths = directories.map { it.path }
            directoryDao.deleteUnwanted(currentDirectoryPaths)
        }

    private fun getDirectoryEntities(
        parentFolder: File? = null,
        currentFolder: File,
        songs: List<Song>,
    ): List<DirectoryEntity> {
        val hasMediaInCurrentFolder = songs.any { it.path.startsWith("${currentFolder.path}/") }
        if (!hasMediaInCurrentFolder) return emptyList()

        val currentDirectoryEntity = DirectoryEntity(
            path = currentFolder.path,
            name = currentFolder.prettyName,
            modified = currentFolder.lastModified(),
            parentPath = parentFolder?.path ?: "/",
        )

        val subDirectories = currentFolder.listFiles { file ->
            file.isDirectory && songs.any { it.path.startsWith(file.path) }
        }?.flatMap { file ->
            getDirectoryEntities(
                parentFolder = currentFolder,
                currentFolder = file,
                songs = songs,
            )
        } ?: emptyList()

        return listOf(currentDirectoryEntity) + subDirectories
    }

    private suspend fun updateSongs(songs: List<Song>) = withContext(Dispatchers.Default) {
        val songEntities = songs.map { song ->
            SongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                duration = song.duration,
                uri = song.uri.toString(),
                albumId = song.albumId,
                trackNumber = song.trackNumber,
                year = song.year,
                path = song.path
            )
        }

        songDao.insertSongs(songEntities)

        val currentSongIds = songEntities.map { it.id }
        songDao.deleteUnwanted(currentSongIds)
    }

    private fun getMediaSongsFlow(): Flow<List<Song>> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(getMediaSongs())
            }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
        // initial value
        trySend(getMediaSongs())
        // close
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.flowOn(dispatcher).distinctUntilChanged()

    private fun getMediaSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                songs.add(
                    Song(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown",
                        album = cursor.getString(albumCol) ?: "Unknown",
                        duration = cursor.getLong(durationCol),
                        uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id),
                        albumId = cursor.getLong(albumIdCol),
                        trackNumber = cursor.getInt(trackCol),
                        year = cursor.getInt(yearCol),
                        path = cursor.getString(pathCol) ?: ""
                    ),
                )
            }
        }
        return songs.filter { File(it.path).exists() }
    }
}
