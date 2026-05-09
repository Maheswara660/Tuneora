package com.maheswara660.tuneora.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "directories")
data class DirectoryEntity(
    @PrimaryKey val path: String,
    val name: String,
    val parentPath: String,
    val modified: Long
)
