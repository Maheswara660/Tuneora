package com.maheswara660.tuneora.core.common.model

data class Folder(
    val name: String,
    val path: String,
    val parentPath: String,
    val modified: Long
)
