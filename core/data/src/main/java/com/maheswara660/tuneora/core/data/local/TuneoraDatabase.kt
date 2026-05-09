package com.maheswara660.tuneora.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.maheswara660.tuneora.core.data.local.dao.DirectoryDao
import com.maheswara660.tuneora.core.data.local.dao.PlaylistDao
import com.maheswara660.tuneora.core.data.local.dao.SearchHistoryDao
import com.maheswara660.tuneora.core.data.local.dao.SongDao
import com.maheswara660.tuneora.core.data.local.entity.DirectoryEntity
import com.maheswara660.tuneora.core.data.local.entity.PlaylistEntity
import com.maheswara660.tuneora.core.data.local.entity.PlaylistSongEntity
import com.maheswara660.tuneora.core.data.local.entity.SearchHistoryEntity
import com.maheswara660.tuneora.core.data.local.entity.SongEntity


@Database(
    entities = [
        SongEntity::class,
        SearchHistoryEntity::class,
        DirectoryEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class TuneoraDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun directoryDao(): DirectoryDao
    abstract fun playlistDao(): PlaylistDao
}



