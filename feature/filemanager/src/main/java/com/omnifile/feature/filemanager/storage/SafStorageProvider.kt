package com.omnifile.feature.filemanager.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.omnifile.feature.filemanager.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class SafStorageProvider(private val context: Context) : StorageProvider {
    override val providerId: String = "saf_provider"

    private val contentResolver = context.contentResolver

    override suspend fun canHandle(uri: Uri): Boolean {
        return uri.scheme == "content"
    }

    override suspend fun list(uri: Uri): List<FileEntry> = withContext(Dispatchers.IO) {
        val rootDoc = try {
            if (uri.toString().contains("tree")) {
                DocumentFile.fromTreeUri(context, uri)
            } else {
                DocumentFile.fromSingleUri(context, uri)
            }
        } catch (e: Exception) {
            null
        } ?: return@withContext emptyList()

        if (!rootDoc.isDirectory) return@withContext emptyList()

        val children = rootDoc.listFiles()
        children.mapNotNull { doc ->
            try {
                toFileEntry(doc)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getMetadata(uri: Uri): FileMetadata? = withContext(Dispatchers.IO) {
        val doc = try {
            DocumentFile.fromSingleUri(context, uri)
        } catch (e: Exception) {
            null
        } ?: return@withContext null

        val entry = toFileEntry(doc)
        FileMetadata(
            entry = entry,
            formattedSize = entry.formattedSize,
            formattedDate = entry.formattedDate,
            childCount = if (doc.isDirectory) doc.listFiles().size else null,
            absolutePathOrUri = doc.uri.toString()
        )
    }

    override suspend fun searchRecursive(rootUri: Uri, query: String, maxDepth: Int): List<FileEntry> = withContext(Dispatchers.IO) {
        val rootDoc = try {
            if (rootUri.toString().contains("tree")) {
                DocumentFile.fromTreeUri(context, rootUri)
            } else {
                DocumentFile.fromSingleUri(context, rootUri)
            }
        } catch (e: Exception) {
            null
        } ?: return@withContext emptyList()

        val results = mutableListOf<FileEntry>()
        val lowerQuery = query.lowercase().trim()

        fun traverse(doc: DocumentFile, currentDepth: Int) {
            if (currentDepth > maxDepth) return
            val files = doc.listFiles()
            for (f in files) {
                val name = f.name ?: continue
                if (name.lowercase().contains(lowerQuery)) {
                    toFileEntry(f).let { results.add(it) }
                }
                if (f.isDirectory) {
                    traverse(f, currentDepth + 1)
                }
            }
        }

        traverse(rootDoc, 1)
        results
    }

    override suspend fun createDirectory(parentUri: Uri, name: String): FileEntry? = withContext(Dispatchers.IO) {
        val parentDoc = DocumentFile.fromTreeUri(context, parentUri)
            ?: DocumentFile.fromSingleUri(context, parentUri)
            ?: return@withContext null

        val sanitized = sanitizeFileName(name)
        if (sanitized.isBlank()) return@withContext null

        val newDir = parentDoc.createDirectory(sanitized) ?: return@withContext null
        toFileEntry(newDir)
    }

    override suspend fun renameEntry(uri: Uri, newName: String): FileEntry? = withContext(Dispatchers.IO) {
        val doc = DocumentFile.fromSingleUri(context, uri)
            ?: DocumentFile.fromTreeUri(context, uri)
            ?: return@withContext null

        val sanitized = sanitizeFileName(newName)
        if (sanitized.isBlank()) return@withContext null

        val success = doc.renameTo(sanitized)
        if (success) {
            toFileEntry(doc)
        } else {
            null
        }
    }

    override fun deleteEntries(uris: List<Uri>): Flow<Pair<OperationProgress, FileOperationResult?>> = flow {
        val total = uris.size
        var deletedCount = 0
        val errors = mutableListOf<String>()

        emit(Pair(OperationProgress(FileOperationType.DELETE, totalItems = total), null))

        for ((index, uri) in uris.withIndex()) {
            val doc = DocumentFile.fromSingleUri(context, uri) ?: DocumentFile.fromTreeUri(context, uri)
            val name = doc?.name ?: uri.lastPathSegment ?: "Item"

            emit(Pair(OperationProgress(FileOperationType.DELETE, currentItemName = name, itemsProcessed = index, totalItems = total), null))

            if (doc != null && doc.exists()) {
                val success = doc.delete()
                if (success) {
                    deletedCount++
                } else {
                    errors.add("Gagal menghapus: $name")
                }
            } else {
                errors.add("Tidak ditemukan: $name")
            }
        }

        val result = if (errors.isEmpty()) {
            FileOperationResult.Success(deletedCount, "Berhasil menghapus $deletedCount file")
        } else if (deletedCount > 0) {
            FileOperationResult.PartialSuccess(deletedCount, errors.size, errors)
        } else {
            FileOperationResult.Failure(errors.joinToString(", "))
        }

        emit(Pair(OperationProgress(FileOperationType.DELETE, itemsProcessed = total, totalItems = total), result))
    }.flowOn(Dispatchers.IO)

    override fun copyEntries(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        conflictStrategy: ConflictStrategy
    ): Flow<Pair<OperationProgress, FileOperationResult?>> = flow {
        val targetParent = DocumentFile.fromTreeUri(context, targetParentUri)
            ?: throw IllegalArgumentException("Direktori tujuan tidak valid atau tidak memiliki akses")

        val total = sourceUris.size
        var copiedCount = 0
        val errors = mutableListOf<String>()

        emit(Pair(OperationProgress(FileOperationType.COPY, totalItems = total), null))

        for ((index, uri) in sourceUris.withIndex()) {
            val sourceDoc = DocumentFile.fromSingleUri(context, uri) ?: DocumentFile.fromTreeUri(context, uri)
            if (sourceDoc == null || !sourceDoc.exists()) {
                errors.add("File sumber tidak ada: $uri")
                continue
            }

            val sourceName = sourceDoc.name ?: "file"
            emit(Pair(OperationProgress(FileOperationType.COPY, currentItemName = sourceName, itemsProcessed = index, totalItems = total), null))

            try {
                copyRecursively(sourceDoc, targetParent, conflictStrategy)
                copiedCount++
            } catch (ce: CancellationException) {
                emit(Pair(OperationProgress(FileOperationType.COPY, itemsProcessed = index, totalItems = total), FileOperationResult.Cancelled))
                throw ce
            } catch (e: Exception) {
                errors.add("Gagal menyalin $sourceName: ${e.message}")
            }
        }

        val result = if (errors.isEmpty()) {
            FileOperationResult.Success(copiedCount, "Berhasil menyalin $copiedCount item")
        } else if (copiedCount > 0) {
            FileOperationResult.PartialSuccess(copiedCount, errors.size, errors)
        } else {
            FileOperationResult.Failure(errors.joinToString(", "))
        }

        emit(Pair(OperationProgress(FileOperationType.COPY, itemsProcessed = total, totalItems = total), result))
    }.flowOn(Dispatchers.IO)

    override fun moveEntries(
        sourceUris: List<Uri>,
        targetParentUri: Uri,
        conflictStrategy: ConflictStrategy
    ): Flow<Pair<OperationProgress, FileOperationResult?>> = flow {
        val targetParent = DocumentFile.fromTreeUri(context, targetParentUri)
            ?: throw IllegalArgumentException("Direktori tujuan tidak valid")

        val total = sourceUris.size
        var movedCount = 0
        val errors = mutableListOf<String>()

        emit(Pair(OperationProgress(FileOperationType.MOVE, totalItems = total), null))

        for ((index, uri) in sourceUris.withIndex()) {
            val sourceDoc = DocumentFile.fromSingleUri(context, uri) ?: DocumentFile.fromTreeUri(context, uri)
            if (sourceDoc == null || !sourceDoc.exists()) {
                errors.add("File sumber tidak ada: $uri")
                continue
            }

            val sourceName = sourceDoc.name ?: "file"
            emit(Pair(OperationProgress(FileOperationType.MOVE, currentItemName = sourceName, itemsProcessed = index, totalItems = total), null))

            try {
                val copiedDoc = copyRecursively(sourceDoc, targetParent, conflictStrategy)
                if (copiedDoc != null && copiedDoc.exists()) {
                    val deleted = sourceDoc.delete()
                    if (deleted) {
                        movedCount++
                    } else {
                        errors.add("Tersalin tapi sumber gagal dihapus: $sourceName")
                    }
                } else {
                    errors.add("Gagal memverifikasi lokasi target untuk: $sourceName")
                }
            } catch (ce: CancellationException) {
                emit(Pair(OperationProgress(FileOperationType.MOVE, itemsProcessed = index, totalItems = total), FileOperationResult.Cancelled))
                throw ce
            } catch (e: Exception) {
                errors.add("Gagal memindahkan $sourceName: ${e.message}")
            }
        }

        val result = if (errors.isEmpty()) {
            FileOperationResult.Success(movedCount, "Berhasil memindahkan $movedCount item")
        } else if (movedCount > 0) {
            FileOperationResult.PartialSuccess(movedCount, errors.size, errors)
        } else {
            FileOperationResult.Failure(errors.joinToString(", "))
        }

        emit(Pair(OperationProgress(FileOperationType.MOVE, itemsProcessed = total, totalItems = total), result))
    }.flowOn(Dispatchers.IO)

    private fun copyRecursively(
        source: DocumentFile,
        destinationDir: DocumentFile,
        conflictStrategy: ConflictStrategy
    ): DocumentFile? {
        val originalName = source.name ?: "unnamed_file"
        var finalName = originalName

        val existing = destinationDir.findFile(originalName)
        if (existing != null && existing.exists()) {
            when (conflictStrategy) {
                ConflictStrategy.CANCEL -> return null
                ConflictStrategy.OVERWRITE -> existing.delete()
                ConflictStrategy.AUTO_RENAME -> finalName = generateConflictFreeName(destinationDir, originalName)
            }
        }

        return if (source.isDirectory) {
            val newDir = destinationDir.createDirectory(finalName)
                ?: throw IllegalStateException("Gagal membuat direktori tujuan: $finalName")
            for (child in source.listFiles()) {
                copyRecursively(child, newDir, conflictStrategy)
            }
            newDir
        } else {
            val mimeType = source.type ?: "application/octet-stream"
            val newFile = destinationDir.createFile(mimeType, finalName)
                ?: throw IllegalStateException("Gagal membuat file tujuan: $finalName")

            var input: InputStream? = null
            var output: OutputStream? = null
            try {
                input = contentResolver.openInputStream(source.uri)
                    ?: throw IllegalStateException("Tidak dapat membaca: ${source.uri}")
                output = contentResolver.openOutputStream(newFile.uri)
                    ?: throw IllegalStateException("Tidak dapat menulis: ${newFile.uri}")

                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
                newFile
            } catch (e: Exception) {
                newFile.delete()
                throw e
            } finally {
                input?.close()
                output?.close()
            }
        }
    }

    private fun generateConflictFreeName(parent: DocumentFile, name: String): String {
        val dotIndex = name.lastIndexOf('.')
        val baseName = if (dotIndex > 0) name.substring(0, dotIndex) else name
        val extension = if (dotIndex > 0) name.substring(dotIndex) else ""

        var counter = 1
        var candidate = "$baseName ($counter)$extension"
        while (parent.findFile(candidate) != null) {
            counter++
            candidate = "$baseName ($counter)$extension"
        }
        return candidate
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[/\\\\:*?\"<>|]"), "_").trim()
    }

    override suspend fun getPersistedPermissionUris(): List<Uri> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.persistedUriPermissions
                .filter { it.isReadPermission }
                .map { it.uri }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun takePersistablePermission(treeUri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(treeUri, flags)
        } catch (_: Exception) {}
    }

    private fun toFileEntry(doc: DocumentFile): FileEntry {
        val name = doc.name ?: "Unknown"
        val isDir = doc.isDirectory
        val mime = doc.type
        val size = if (isDir) 0L else doc.length()
        val lastMod = doc.lastModified()
        val ext = name.substringAfterLast('.', "")
        val category = FileEntry.determineCategory(name, isDir, mime)

        return FileEntry(
            id = doc.uri.toString(),
            name = name,
            path = doc.uri.path ?: name,
            uri = doc.uri,
            mimeType = mime,
            isDirectory = isDir,
            sizeBytes = size,
            lastModifiedMillis = lastMod,
            isReadable = doc.canRead(),
            isWritable = doc.canWrite(),
            extension = ext,
            category = category
        )
    }
}
