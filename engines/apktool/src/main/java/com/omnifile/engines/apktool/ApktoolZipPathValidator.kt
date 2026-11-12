package com.omnifile.engines.apktool

import java.io.File

/** Validates untrusted ZIP entry names before they are used as filesystem paths. */
internal object ApktoolZipPathValidator {
    private const val MAX_PATH_LENGTH = 4096
    private const val MAX_SEGMENT_LENGTH = 255

    /**
     * Resolve [entryName] beneath [destinationRoot], rejecting traversal and
     * platform-specific absolute/alternate path forms. Canonical resolution also
     * detects pre-existing symlink components that point outside the root.
     */
    fun resolveEntry(destinationRoot: File, entryName: String): File {
        require(entryName.isNotEmpty()) { "Nama entry kosong" }
        require(entryName.length <= MAX_PATH_LENGTH) { "Path entry terlalu panjang" }
        require('\u0000' !in entryName) { "Path entry mengandung NUL" }
        require('\\' !in entryName) { "Pemisah backslash tidak diizinkan" }
        require(!entryName.startsWith('/')) { "Path absolut tidak diizinkan" }
        require(!Regex("^[A-Za-z]:").containsMatchIn(entryName)) { "Path drive absolut tidak diizinkan" }

        val isDirectory = entryName.endsWith('/')
        val relative = if (isDirectory) entryName.dropLast(1) else entryName
        require(relative.isNotEmpty()) { "Path entry tidak menunjuk file/folder" }

        val segments = relative.split('/')
        require(segments.all { segment ->
            segment.isNotEmpty() && segment != "." && segment != ".." &&
                segment.length <= MAX_SEGMENT_LENGTH && ':' !in segment
        }) { "Path traversal atau segmen path tidak valid" }

        val canonicalRoot = destinationRoot.canonicalFile
        val candidate = File(canonicalRoot, relative).canonicalFile
        val rootPrefix = canonicalRoot.path.trimEnd(File.separatorChar) + File.separator
        require(candidate.path.startsWith(rootPrefix)) {
            "Entry keluar dari direktori tujuan"
        }
        return candidate
    }
}
