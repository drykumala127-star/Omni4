package com.omnifile.feature.filemanager.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.omnifile.feature.filemanager.contract.IFileRepository
import com.omnifile.feature.filemanager.model.*
import com.omnifile.feature.filemanager.storage.BookmarkPreferences
import com.omnifile.feature.filemanager.storage.SafStorageProvider
import com.omnifile.feature.filemanager.zip.ZipManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class FileRepositoryImpl(private val context: Context) : IFileRepository {

    private val safProvider = SafStorageProvider(context)
    private val zipManager = ZipManager(context)
    private val bookmarkPrefs = BookmarkPreferences(context)

    override suspend fun listFiles(
        directoryUri: Uri,
        sortOrder: SortOrder,
        filterCriteria: FilterCriteria
    ): Result<List<FileEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            val rawList = safProvider.list(directoryUri)
            val filtered = rawList.filter { filterCriteria.matches(it) }
            sortFiles(filtered, sortOrder)
        }
    }

    override suspend fun getFileMetadata(fileUri: Uri): Result<FileMetadata> =
        withContext(Dispatchers.IO) {
            runCatching {
                safProvider.getMetadata(fileUri)
                    ?: throw NoSuchElementException("File metadata not found for URI: $fileUri")
            }
        }

    override suspend fun search(
        query: String,
        rootUri: Uri,
        recursive: Boolean,
        filterCriteria: FilterCriteria
    ): Result<List<FileEntry>> = withContext(Dispatchers.IO) {
        runCatching {
            val allFiles = if (recursive) {
                safProvider.searchRecursive(rootUri, query)
            } else {
                safProvider.list(rootUri).filter { it.name.contains(query, ignoreCase = true) }
            }
            allFiles.filter { filterCriteria.matches(it) }
        }
    }

    override suspend fun createFolder(parentUri: Uri, folderName: String): Result<FileEntry> =
        withContext(Dispatchers.IO) {
            runCatching {
                safProvider.createDirectory(parentUri, folderName)
                    ?: throw IllegalStateException("Gagal membuat folder: $folderName")
            }
        }

    override suspend fun rename(targetUri: Uri, newName: String): Result<FileEntry> =
        withContext(Dispatchers.IO) {
            runCatching {
                safProvider.renameEntry(targetUri, newName)
                    ?: throw IllegalStateException("Gagal mengubah nama menjadi: $newName")
            }
        }

    override fun delete(targetUris: List<Uri>): Flow<Pair<OperationProgress, FileOperationResult?>> =
        safProvider.deleteEntries(targetUris).flowOn(Dispatchers.IO)

    override fun copy(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        conflictStrategy: ConflictStrategy
    ): Flow<Pair<OperationProgress, FileOperationResult?>> =
        safProvider.copyEntries(sourceUris, targetParentUri, conflictStrategy).flowOn(Dispatchers.IO)

    override fun move(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        conflictStrategy: ConflictStrategy
    ): Flow<Pair<OperationProgress, FileOperationResult?>> =
        safProvider.moveEntries(sourceUris, targetParentUri, conflictStrategy).flowOn(Dispatchers.IO)

    override fun extractZip(
        zipUri: Uri,
        destinationDirUri: Uri
    ): Flow<Pair<OperationProgress, FileOperationResult?>> {
        val destDoc = DocumentFile.fromTreeUri(context, destinationDirUri)
            ?: DocumentFile.fromSingleUri(context, destinationDirUri)
            ?: throw IllegalArgumentException("Direktori tujuan ekstraksi tidak valid: $destinationDirUri")
        return zipManager.extract(zipUri, destDoc).flowOn(Dispatchers.IO)
    }

    override fun compressZip(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        zipFileName: String
    ): Flow<Pair<OperationProgress, FileOperationResult?>> {
        val targetParentDoc = DocumentFile.fromTreeUri(context, targetParentUri)
            ?: DocumentFile.fromSingleUri(context, targetParentUri)
            ?: throw IllegalArgumentException("Direktori tujuan penyimpanan ZIP tidak valid: $targetParentUri")
        return zipManager.compress(sourceUris, targetParentDoc, zipFileName).flowOn(Dispatchers.IO)
    }

    override fun getFavorites(): Flow<List<BookmarkItem>> {
        return bookmarkPrefs.favoritesFlow
    }

    override fun toggleFavorite(uri: Uri, name: String, isDirectory: Boolean) {
        bookmarkPrefs.toggleFavorite(uri.toString(), name, isDirectory)
    }

    override fun isFavorite(uri: Uri): Boolean {
        return bookmarkPrefs.isFavorite(uri.toString())
    }

    override fun getRecentAccessRecords(): Flow<List<RecentAccessRecord>> {
        return bookmarkPrefs.recentsFlow
    }

    override fun recordRecentAccess(uri: Uri, name: String, isDirectory: Boolean, sizeBytes: Long) {
        bookmarkPrefs.recordRecentAccess(uri.toString(), name, isDirectory, sizeBytes)
    }

    private fun sortFiles(files: List<FileEntry>, sortOrder: SortOrder): List<FileEntry> {
        val comparator = when (sortOrder.field) {
            SortField.NAME -> compareBy<FileEntry> { it.name.lowercase() }
            SortField.SIZE -> compareBy<FileEntry> { it.sizeBytes }
            SortField.DATE -> compareBy<FileEntry> { it.lastModifiedMillis }
            SortField.TYPE -> compareBy<FileEntry> { it.extension }
        }

        val directedComparator = if (sortOrder.direction == SortDirection.DESCENDING) {
            comparator.reversed()
        } else {
            comparator
        }

        return files.sortedWith(
            compareByDescending<FileEntry> { it.isDirectory }.then(directedComparator)
        )
    }

    fun getPersistedUris(): List<Uri> {
        return context.contentResolver.persistedUriPermissions
            .filter { it.isReadPermission }
            .map { it.uri }
    }

    fun takePersistablePermission(treeUri: Uri) {
        safProvider.takePersistablePermission(treeUri)
    }
}
