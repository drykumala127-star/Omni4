package com.omnifile.feature.filemanager.storage

import android.net.Uri
import com.omnifile.feature.filemanager.model.*
import kotlinx.coroutines.flow.Flow

interface StorageProvider {
    val providerId: String
    suspend fun canHandle(uri: Uri): Boolean
    suspend fun list(uri: Uri): List<FileEntry>
    suspend fun getMetadata(uri: Uri): FileMetadata?
    suspend fun getPersistedPermissionUris(): List<Uri>

    suspend fun searchRecursive(rootUri: Uri, query: String, maxDepth: Int = 5): List<FileEntry>

    suspend fun createDirectory(parentUri: Uri, name: String): FileEntry?
    suspend fun renameEntry(uri: Uri, newName: String): FileEntry?

    fun deleteEntries(
        uris: List<Uri>
    ): Flow<Pair<OperationProgress, FileOperationResult?>>

    fun copyEntries(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        conflictStrategy: ConflictStrategy
    ): Flow<Pair<OperationProgress, FileOperationResult?>>

    fun moveEntries(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        conflictStrategy: ConflictStrategy
    ): Flow<Pair<OperationProgress, FileOperationResult?>>
}
