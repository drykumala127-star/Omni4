package com.omnifile.feature.filemanager.zip

import com.omnifile.feature.filemanager.model.ZipSecurityException
import java.io.File

/**
 * Robust security auditor to neutralize Zip Slip vulnerabilities and Zip Bomb DoS attacks.
 */
object ZipSecurityValidator {

    const val MAX_ENTRIES_DEFAULT = 10_000
    const val MAX_UNCOMPRESSED_BYTES_DEFAULT = 2L * 1024L * 1024L * 1024L // 2 GB
    const val MAX_COMPRESSION_RATIO_DEFAULT = 100 // Flag ratio > 100:1

    /**
     * Inspects raw zip entry name for directory traversal patterns, null bytes, and illegal prefixes.
     * Returns sanitized relative path string or throws ZipSecurityException.
     */
    fun validateAndSanitizeEntryName(entryName: String): String {
        if (entryName.contains('\u0000')) {
            throw ZipSecurityException("Entry contains poisoned null byte: $entryName")
        }

        // Normalize slashes
        val normalized = entryName.replace('\\', '/')

        // Check for directory traversal sequences
        val segments = normalized.split('/')
        for (segment in segments) {
            if (segment == "..") {
                throw ZipSecurityException("Zip Slip directory traversal pattern ('..') detected: $entryName")
            }
        }

        // Must not start with root slash
        val stripped = normalized.trimStart('/')
        if (stripped.isEmpty() && !normalized.endsWith('/')) {
            throw ZipSecurityException("Empty or invalid relative entry path: $entryName")
        }

        return stripped
    }

    /**
     * Asserts that the canonical path of the target destination file remains strictly inside
     * the canonical path of the root extraction directory.
     */
    fun assertInsideDestination(targetFile: File, destinationDir: File) {
        val destCanonical = destinationDir.canonicalPath
        val targetCanonical = targetFile.canonicalPath

        val prefixWithSeparator = if (destCanonical.endsWith(File.separator)) {
            destCanonical
        } else {
            destCanonical + File.separator
        }

        // Exact match or subpath check
        if (targetCanonical != destCanonical && !targetCanonical.startsWith(prefixWithSeparator)) {
            throw ZipSecurityException("Zip Slip blocked! Path traversal escapes destination directory. Target: $targetCanonical vs Destination: $destCanonical")
        }
    }

    /**
     * Monitors streaming decompression to protect against resource exhaustion (Zip Bomb).
     */
    fun checkZipBombProtection(
        entriesCount: Int,
        totalUncompressedBytes: Long,
        totalCompressedBytes: Long,
        maxEntries: Int = MAX_ENTRIES_DEFAULT,
        maxUncompressedBytes: Long = MAX_UNCOMPRESSED_BYTES_DEFAULT
    ) {
        if (entriesCount > maxEntries) {
            throw ZipSecurityException("Zip Bomb Defense: Exceeded maximum allowed entry count ($entriesCount > $maxEntries)")
        }
        if (totalUncompressedBytes > maxUncompressedBytes) {
            throw ZipSecurityException("Zip Bomb Defense: Exceeded maximum allowed uncompressed size ($totalUncompressedBytes bytes > $maxUncompressedBytes bytes)")
        }

        // Ratio inspection (only active after a reasonable sample size > 2MB)
        if (totalCompressedBytes > 2L * 1024L * 1024L && totalUncompressedBytes > 0L) {
            val ratio = totalUncompressedBytes / totalCompressedBytes
            if (ratio > MAX_COMPRESSION_RATIO_DEFAULT) {
                throw ZipSecurityException("Zip Bomb Defense: Dangerous compression ratio detected ($ratio:1)")
            }
        }
    }
}
