package com.maheswara660.tuneora.core.data.repository

import com.maheswara660.tuneora.core.data.local.dao.SearchHistoryDao
import com.maheswara660.tuneora.core.data.local.entity.SearchHistoryEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class LocalSearchHistoryRepository @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao
) : SearchHistoryRepository {

    override val searchHistory: Flow<List<String>> = searchHistoryDao.getSearchHistory()

    override suspend fun addSearchQuery(query: String) {
        searchHistoryDao.upsert(SearchHistoryEntity(query))
    }

    override suspend fun removeSearchQuery(query: String) {
        searchHistoryDao.delete(query)
    }

    override suspend fun clearHistory() {
        searchHistoryDao.clearAll()
    }
}
