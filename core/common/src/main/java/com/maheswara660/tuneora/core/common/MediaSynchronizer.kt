package com.maheswara660.tuneora.core.common

interface MediaSynchronizer {
    suspend fun refresh(path: String? = null): Boolean
    suspend fun delete(songs: List<com.maheswara660.tuneora.core.common.model.Song>): Boolean
    fun startSync()
    fun stopSync()
}
