package com.omnifile.app

import com.omnifile.feature.filemanager.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Architecture & Unit tests verifying module boundaries, data contracts, and file operation safety rules.
 */
class OmniFileArchitectureTest {

    @Test
    fun verifyFileCategoryClassification() {
        assertEquals(FileCategory.ARCHIVE_APK, FileEntry.determineCategory("app.apk", false, "application/vnd.android.package-archive"))
        assertEquals(FileCategory.ARCHIVE_ZIP, FileEntry.determineCategory("archive.zip", false, "application/zip"))
        assertEquals(FileCategory.CODE_SOURCE, FileEntry.determineCategory("MainActivity.kt", false, "text/plain"))
        assertEquals(FileCategory.CODE_SOURCE, FileEntry.determineCategory("Build.gradle.kts", false, "text/plain"))
        assertEquals(FileCategory.DOCUMENT, FileEntry.determineCategory("AndroidManifest.xml", false, "text/xml"))
        assertEquals(FileCategory.UNKNOWN, FileEntry.determineCategory("folder", true, null))
    }

    @Test
    fun verifyByteFormatting() {
        assertEquals("0 B", FileEntry.formatBytes(0L, false))
        assertEquals("500.0 B", FileEntry.formatBytes(500L, false))
        assertEquals("1.0 KB", FileEntry.formatBytes(1024L, false))
        assertEquals("1.5 MB", FileEntry.formatBytes(1572864L, false))
        assertEquals("Folder", FileEntry.formatBytes(1024L, true))
    }

    @Test
    fun verifyFileNameSanitizationRule() {
        val illegalName = "test/file:name*?.apk"
        val sanitized = illegalName.replace(Regex("[/\\\\:*?\"<>|]"), "_").trim()
        assertEquals("test_file_name__.apk", sanitized)
        assertFalse(sanitized.contains("/"))
        assertFalse(sanitized.contains(":"))
        assertFalse(sanitized.contains("*"))
        assertFalse(sanitized.contains("?"))
    }

    @Test
    fun verifyConflictFreeNameGenerationLogic() {
        fun resolveCandidate(existingNames: Set<String>, name: String): String {
            val dotIndex = name.lastIndexOf('.')
            val baseName = if (dotIndex > 0) name.substring(0, dotIndex) else name
            val extension = if (dotIndex > 0) name.substring(dotIndex) else ""

            var counter = 1
            var candidate = "$baseName ($counter)$extension"
            while (existingNames.contains(candidate)) {
                counter++
                candidate = "$baseName ($counter)$extension"
            }
            return candidate
        }

        val existing = setOf("app.apk", "app (1).apk", "app (2).apk")
        val nextName = resolveCandidate(existing, "app.apk")
        assertEquals("app (3).apk", nextName)
    }

    @Test
    fun verifyCategoryFilterMatching() {
        val apkCriteria = FilterCriteria(categoryFilter = FileCategoryFilter.APK)
        val apkEntry = FileEntry(
            id = "1", name = "base.apk", path = "base.apk",
            uri = android.net.Uri.EMPTY, mimeType = null, isDirectory = false,
            sizeBytes = 1024L, lastModifiedMillis = 0L, category = FileCategory.ARCHIVE_APK
        )
        val zipEntry = FileEntry(
            id = "2", name = "base.zip", path = "base.zip",
            uri = android.net.Uri.EMPTY, mimeType = null, isDirectory = false,
            sizeBytes = 1024L, lastModifiedMillis = 0L, category = FileCategory.ARCHIVE_ZIP
        )
        assertTrue(apkCriteria.matches(apkEntry))
        assertFalse(apkCriteria.matches(zipEntry))
    }

    @Test
    fun verifySortingComparatorLogic() {
        val fileA = FileEntry("1", "alpha.txt", "alpha.txt", android.net.Uri.EMPTY, null, false, 100L, 1000L, category = FileCategory.DOCUMENT)
        val fileB = FileEntry("2", "beta.txt", "beta.txt", android.net.Uri.EMPTY, null, false, 500L, 500L, category = FileCategory.DOCUMENT)
        val dir = FileEntry("3", "folder", "folder", android.net.Uri.EMPTY, null, true, 0L, 2000L, category = FileCategory.UNKNOWN)

        val list = listOf(fileB, dir, fileA)
        // Directories on top, then sorted by size ascending
        val sortedBySize = list.sortedWith(
            compareByDescending<FileEntry> { it.isDirectory }.thenBy { it.sizeBytes }
        )
        assertEquals("folder", sortedBySize[0].name)
        assertEquals("alpha.txt", sortedBySize[1].name)
        assertEquals("beta.txt", sortedBySize[2].name)
    }
}
