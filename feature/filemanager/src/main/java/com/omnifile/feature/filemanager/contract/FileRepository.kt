package com.omnifile.feature.filemanager.contract

import android.net.Uri
import com.omnifile.feature.filemanager.model.*
import kotlinx.coroutines.flow.Flow

interface IFileRepository {
    /**
     * Lists files and subdirectories within the specified directory URI.
     */
    suspend fun listFiles(
        directoryUri: Uri,
        sortOrder: SortOrder = SortOrder(),
        filterCriteria: FilterCriteria = FilterCriteria()
    ): Result<List<FileEntry>>

    /**
     * Obtains detailed metadata for a specific file URI.
     */
    suspend fun getFileMetadata(fileUri: Uri): Result<FileMetadata>

    /**
     * Searches for files matching the query within the given root URI, with recursive option.
     */
    suspend fun search(
        query: String,
        rootUri: Uri,
        recursive: Boolean = false,
        filterCriteria: FilterCriteria = FilterCriteria()
    ): Result<List<FileEntry>>

    /**
     * Creates a new directory in the given parent URI.
     */
    suspend fun createFolder(parentUri: Uri, folderName: String): Result<FileEntry>

    /**
     * Renames an existing file or directory.
     */
    suspend fun rename(targetUri: Uri, newName: String): Result<FileEntry>

    /**
     * Deletes one or multiple files/directories with operation progress streaming.
     */
    fun delete(
        targetUris: List<Uri>
    ): Flow<Pair<OperationProgress, FileOperationResult?>>

    /**
     * Copies files/directories to the target destination folder.
     */
    fun copy(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        conflictStrategy: ConflictStrategy = ConflictStrategy.AUTO_RENAME
    ): Flow<Pair<OperationProgress, FileOperationResult?>>

    /**
     * Moves files/directories using safe-move verification before source deletion.
     */
    fun move(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        conflictStrategy: ConflictStrategy = ConflictStrategy.AUTO_RENAME
    ): Flow<Pair<OperationProgress, FileOperationResult?>>

    /**
     * Extracts an archive ZIP into destination directory with Zip Slip protection.
     */
    fun extractZip(
        zipUri: Uri,
        destinationDirUri: Uri
    ): Flow<Pair<OperationProgress, FileOperationResult?>>

    /**
     * Compresses multiple selected files/folders into a ZIP archive.
     */
    fun compressZip(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        zipFileName: String
    ): Flow<Pair<OperationProgress, FileOperationResult?>>

    /**
     * Stream of user-pinned favorite bookmarks.
     */
    fun getFavorites(): Flow<List<BookmarkItem>>

    /**
     * Toggles favorite status for a given file or folder.
     */
    fun toggleFavorite(uri: Uri, name: String, isDirectory: Boolean)

    /**
     * Checks if a URI is currently bookmarked.
     */
    fun isFavorite(uri: Uri): Boolean

    /**
     * Stream of recent file access history.
     */
    fun getRecentAccessRecords(): Flow<List<RecentAccessRecord>>

    /**
     * Logs file interaction into recent history.
     */
    fun recordRecentAccess(uri: Uri, name: String, isDirectory: Boolean, sizeBytes: Long)
}
