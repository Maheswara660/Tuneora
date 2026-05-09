package com.maheswara660.tuneora.core.data.repository

import com.maheswara660.tuneora.core.common.model.Folder
import com.maheswara660.tuneora.core.data.local.dao.DirectoryDao
import com.maheswara660.tuneora.core.data.local.entity.DirectoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FolderRepository @Inject constructor(
    private val directoryDao: DirectoryDao
) {
    val directories: Flow<List<Folder>> = directoryDao.getAllDirectories().map { entities ->
        entities.map { it.toModel() }
    }
}

private fun DirectoryEntity.toModel() = Folder(
    name = name,
    path = path,
    parentPath = parentPath,
    modified = modified
)

