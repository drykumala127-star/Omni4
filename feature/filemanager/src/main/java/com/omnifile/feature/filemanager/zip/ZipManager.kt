package com.omnifile.feature.filemanager.zip

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.omnifile.feature.filemanager.model.FileOperationResult
import com.omnifile.feature.filemanager.model.FileOperationType
import com.omnifile.feature.filemanager.model.OperationProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * High-performance streaming ZIP engine with Zip Slip protection, Zip Bomb defense,
 * and safe atomic rollback for Android Storage Access Framework (SAF).
 */
class ZipManager(private val context: Context) {

    private val contentResolver = context.contentResolver

    /**
     * Extracts a ZIP archive URI into the specified target parent directory DocumentFile.
     */
    fun extract(
        zipUri: Uri,
        destinationDir: DocumentFile
    ): Flow<Pair<OperationProgress, FileOperationResult?>> = flow {
        emit(Pair(OperationProgress(FileOperationType.EXTRACT_ZIP, isIndeterminate = true), null))

        val createdDocs = mutableListOf<DocumentFile>()
        var extractedCount = 0
        var totalBytesRead = 0L
        var totalCompressedBytes = 0L

        var zipIn: ZipInputStream? = null
        try {
            val rawIn = contentResolver.openInputStream(zipUri)
                ?: throw IllegalStateException("Tidak dapat membuka file ZIP sumber: $zipUri")
            zipIn = ZipInputStream(BufferedInputStream(rawIn, 64 * 1024))

            var entry = zipIn.nextEntry
            while (entry != null) {
                val rawName = entry.name
                val sanitizedPath = ZipSecurityValidator.validateAndSanitizeEntryName(rawName)

                // Track and enforce security limits
                val compressedSize = if (entry.compressedSize > 0) entry.compressedSize else 1024L
                totalCompressedBytes += compressedSize
                ZipSecurityValidator.checkZipBombProtection(
                    entriesCount = extractedCount + 1,
                    totalUncompressedBytes = totalBytesRead,
                    totalCompressedBytes = totalCompressedBytes
                )

                emit(
                    Pair(
                        OperationProgress(
                            operation = FileOperationType.EXTRACT_ZIP,
                            currentItemName = sanitizedPath,
                            itemsProcessed = extractedCount,
                            bytesProcessed = totalBytesRead,
                            isIndeterminate = true
                        ),
                        null
                    )
                )

                if (entry.isDirectory) {
                    getOrCreateDirectoryPath(destinationDir, sanitizedPath, createdDocs)
                } else {
                    val segments = sanitizedPath.split('/')
                    val parentFolder = if (segments.size > 1) {
                        val folderPath = segments.dropLast(1).joinToString("/")
                        getOrCreateDirectoryPath(destinationDir, folderPath, createdDocs)
                    } else {
                        destinationDir
                    }

                    val fileName = segments.last()
                    val mime = determineMimeType(fileName)
                    val newDoc = parentFolder.createFile(mime, fileName)
                        ?: throw IllegalStateException("Gagal membuat file hasil ekstraksi: $fileName")
                    createdDocs.add(newDoc)

                    var out: OutputStream? = null
                    try {
                        val rawOut = contentResolver.openOutputStream(newDoc.uri)
                            ?: throw IllegalStateException("Tidak dapat menulis file ekstraksi: ${newDoc.uri}")
                        out = BufferedOutputStream(rawOut, 64 * 1024)

                        val buffer = ByteArray(64 * 1024)
                        var readBytes: Int
                        while (zipIn.read(buffer).also { readBytes = it } != -1) {
                            out.write(buffer, 0, readBytes)
                            totalBytesRead += readBytes
                        }
                        out.flush()
                    } finally {
                        out?.close()
                    }
                    extractedCount++
                }

                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }

            emit(
                Pair(
                    OperationProgress(
                        operation = FileOperationType.EXTRACT_ZIP,
                        itemsProcessed = extractedCount,
                        bytesProcessed = totalBytesRead,
                        isIndeterminate = false
                    ),
                    FileOperationResult.Success(extractedCount, "Ekstraksi berhasil: $extractedCount item diekstrak")
                )
            )
        } catch (ce: CancellationException) {
            rollbackCreatedDocs(createdDocs)
            emit(Pair(OperationProgress(FileOperationType.EXTRACT_ZIP), FileOperationResult.Cancelled))
            throw ce
        } catch (e: Exception) {
            rollbackCreatedDocs(createdDocs)
            emit(
                Pair(
                    OperationProgress(FileOperationType.EXTRACT_ZIP),
                    FileOperationResult.Failure("Ekstraksi gagal: ${e.message}", e)
                )
            )
        } finally {
            zipIn?.close()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Compresses multiple source URIs or directories into a single target ZIP file.
     */
    fun compress(
        sourceUris: List<Uri>,
        targetParentDir: DocumentFile,
        zipFileName: String
    ): Flow<Pair<OperationProgress, FileOperationResult?>> = flow {
        emit(Pair(OperationProgress(FileOperationType.COMPRESS_ZIP, isIndeterminate = true), null))

        val finalName = if (zipFileName.lowercase().endsWith(".zip")) zipFileName else "$zipFileName.zip"
        val zipFileDoc = targetParentDir.createFile("application/zip", finalName)
            ?: throw IllegalStateException("Gagal membuat arsip ZIP di direktori tujuan: $finalName")

        var zipOut: ZipOutputStream? = null
        var totalCompressedEntries = 0
        var totalBytesWritten = 0L

        try {
            val rawOut = contentResolver.openOutputStream(zipFileDoc.uri)
                ?: throw IllegalStateException("Tidak dapat menulis ke file ZIP: ${zipFileDoc.uri}")
            zipOut = ZipOutputStream(BufferedOutputStream(rawOut, 64 * 1024))

            for ((index, uri) in sourceUris.withIndex()) {
                val doc = DocumentFile.fromSingleUri(context, uri)
                    ?: DocumentFile.fromTreeUri(context, uri)
                    ?: continue

                emit(
                    Pair(
                        OperationProgress(
                            operation = FileOperationType.COMPRESS_ZIP,
                            currentItemName = doc.name ?: "File",
                            itemsProcessed = index,
                            totalItems = sourceUris.size
                        ),
                        null
                    )
                )

                val count = compressRecursive(doc, "", zipOut) { bytes ->
                    totalBytesWritten += bytes
                }
                totalCompressedEntries += count
            }

            zipOut.finish()
            zipOut.flush()

            emit(
                Pair(
                    OperationProgress(
                        operation = FileOperationType.COMPRESS_ZIP,
                        itemsProcessed = totalCompressedEntries,
                        totalItems = totalCompressedEntries,
                        bytesProcessed = totalBytesWritten
                    ),
                    FileOperationResult.Success(
                        totalCompressedEntries,
                        "Kompresi berhasil: $totalCompressedEntries item dikompres ke $finalName"
                    )
                )
            )
        } catch (ce: CancellationException) {
            zipFileDoc.delete()
            emit(Pair(OperationProgress(FileOperationType.COMPRESS_ZIP), FileOperationResult.Cancelled))
            throw ce
        } catch (e: Exception) {
            zipFileDoc.delete()
            emit(
                Pair(
                    OperationProgress(FileOperationType.COMPRESS_ZIP),
                    FileOperationResult.Failure("Kompresi gagal: ${e.message}", e)
                )
            )
        } finally {
            zipOut?.close()
        }
    }.flowOn(Dispatchers.IO)

    private fun compressRecursive(
        doc: DocumentFile,
        parentPath: String,
        zipOut: ZipOutputStream,
        onBytesWritten: (Int) -> Unit
    ): Int {
        val name = doc.name ?: "unnamed"
        val currentPath = if (parentPath.isEmpty()) name else "$parentPath/$name"

        if (doc.isDirectory) {
            // Write directory entry
            val dirEntry = ZipEntry("$currentPath/")
            dirEntry.time = doc.lastModified()
            zipOut.putNextEntry(dirEntry)
            zipOut.closeEntry()

            var count = 1
            for (child in doc.listFiles()) {
                count += compressRecursive(child, currentPath, zipOut, onBytesWritten)
            }
            return count
        } else {
            val entry = ZipEntry(currentPath)
            entry.time = doc.lastModified()
            zipOut.putNextEntry(entry)

            var inStream: InputStream? = null
            try {
                val rawIn = contentResolver.openInputStream(doc.uri)
                    ?: throw IllegalStateException("Tidak dapat membaca data dari: ${doc.uri}")
                inStream = BufferedInputStream(rawIn, 64 * 1024)

                val buffer = ByteArray(64 * 1024)
                var bytesRead: Int
                while (inStream.read(buffer).also { bytesRead = it } != -1) {
                    zipOut.write(buffer, 0, bytesRead)
                    onBytesWritten(bytesRead)
                }
            } finally {
                inStream?.close()
                zipOut.closeEntry()
            }
            return 1
        }
    }

    private fun getOrCreateDirectoryPath(
        root: DocumentFile,
        relativePath: String,
        createdTracker: MutableList<DocumentFile>
    ): DocumentFile {
        var current = root
        val segments = relativePath.split('/').filter { it.isNotEmpty() }
        for (segment in segments) {
            val existing = current.findFile(segment)
            current = if (existing != null && existing.isDirectory) {
                existing
            } else {
                val created = current.createDirectory(segment)
                    ?: throw IllegalStateException("Gagal membuat direktori bersarang: $segment")
                createdTracker.add(created)
                created
            }
        }
        return current
    }

    private fun rollbackCreatedDocs(createdDocs: List<DocumentFile>) {
        // Delete in reverse order to remove children before parent folders
        for (doc in createdDocs.asReversed()) {
            try {
                if (doc.exists()) {
                    doc.delete()
                }
            } catch (_: Exception) {}
        }
    }

    private fun determineMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "txt" -> "text/plain"
            "html", "htm" -> "text/html"
            "xml" -> "text/xml"
            "json" -> "application/json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "apk" -> "application/vnd.android.package-archive"
            "zip" -> "application/zip"
            "dex" -> "application/octet-stream"
            else -> "application/octet-stream"
        }
    }
}
