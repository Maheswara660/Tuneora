package com.maheswara660.tuneora.core.data.local.dao

import androidx.room.*
import com.maheswara660.tuneora.core.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT `query` FROM search_history ORDER BY timestamp DESC")
    fun getSearchHistory(): Flow<List<String>>

    @Upsert
    suspend fun upsert(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE `query` = :query")
    suspend fun delete(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
