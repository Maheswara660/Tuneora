package com.maheswara660.tuneora.core.data.local.dao

import androidx.room.*
import com.maheswara660.tuneora.core.data.local.entity.DirectoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectoryDao {
    @Query("SELECT * FROM directories ORDER BY name ASC")
    fun getAllDirectories(): Flow<List<DirectoryEntity>>

    @Upsert
    suspend fun upsertAll(directories: List<DirectoryEntity>)

    @Query("DELETE FROM directories WHERE path NOT IN (:paths)")
    suspend fun deleteUnwanted(paths: List<String>)

    @Query("DELETE FROM directories")
    suspend fun deleteAll()
}
