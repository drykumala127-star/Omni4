package com.omnifile.feature.filemanager

data class FileEntry(
    val name: String,
    val uri: String,
    val mimeType: String?,
    val isDirectory: Boolean,
    val sizeBytes: Long? = null
)
