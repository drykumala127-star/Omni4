package com.omnifile.feature.filemanager.model

/**
 * Filter categories for quick filtering in the file explorer.
 */
enum class FileCategoryFilter(val label: String) {
    ALL("Semua"),
    APK("APK"),
    ARCHIVE("Arsip / ZIP"),
    CODE("Kode Sumber"),
    DOCUMENTS("Dokumen"),
    MEDIA("Gambar & Video")
}

/**
 * Criteria model combining category filter, extension filter, and size restrictions.
 */
data class FilterCriteria(
    val categoryFilter: FileCategoryFilter = FileCategoryFilter.ALL,
    val showHiddenFiles: Boolean = false,
    val minSizeBytes: Long? = null,
    val maxSizeBytes: Long? = null
) {
    fun matches(entry: FileEntry): Boolean {
        if (!showHiddenFiles && entry.name.startsWith(".")) {
            return false
        }

        // Directories are always shown unless category-specific filtering is applied
        if (entry.isDirectory && categoryFilter != FileCategoryFilter.ALL) {
            return false
        }

        val categoryMatch = when (categoryFilter) {
            FileCategoryFilter.ALL -> true
            FileCategoryFilter.APK -> entry.category == FileCategory.ARCHIVE_APK
            FileCategoryFilter.ARCHIVE -> entry.category == FileCategory.ARCHIVE_ZIP
            FileCategoryFilter.CODE -> entry.category == FileCategory.CODE_SOURCE
            FileCategoryFilter.DOCUMENTS -> entry.category == FileCategory.DOCUMENT
            FileCategoryFilter.MEDIA -> entry.category == FileCategory.IMAGE || entry.category == FileCategory.VIDEO || entry.category == FileCategory.AUDIO
        }

        if (!categoryMatch) return false

        if (minSizeBytes != null && entry.sizeBytes < minSizeBytes) return false
        if (maxSizeBytes != null && entry.sizeBytes > maxSizeBytes) return false

        return true
    }
}
