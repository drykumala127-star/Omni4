package com.omnifile.feature.filemanager.model

import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FileCategory {
    ARCHIVE_APK,
    ARCHIVE_ZIP,
    CODE_SOURCE,
    IMAGE,
    DOCUMENT,
    AUDIO,
    VIDEO,
    UNKNOWN
}

data class FileEntry(
    val id: String,
    val name: String,
    val path: String,
    val uri: Uri,
    val mimeType: String?,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val isReadable: Boolean = true,
    val isWritable: Boolean = true,
    val extension: String = "",
    val category: FileCategory = FileCategory.UNKNOWN
) {
    val formattedSize: String get() = formatBytes(sizeBytes, isDirectory)
    val formattedDate: String get() = formatDate(lastModifiedMillis)

    companion object {
        fun determineCategory(name: String, isDirectory: Boolean, mimeType: String?): FileCategory {
            if (isDirectory) return FileCategory.UNKNOWN
            val ext = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
            return when {
                ext == "apk" -> FileCategory.ARCHIVE_APK
                ext in listOf("zip", "jar", "tar", "gz", "7z", "rar") -> FileCategory.ARCHIVE_ZIP
                ext in listOf("kt", "java", "xml", "json", "smali", "cpp", "c", "h", "gradle", "kts", "py", "sh") -> FileCategory.CODE_SOURCE
                ext in listOf("png", "jpg", "jpeg", "webp", "gif", "svg") || (mimeType?.startsWith("image/") == true) -> FileCategory.IMAGE
                ext in listOf("pdf", "txt", "md", "doc", "docx", "xls", "xlsx") || (mimeType?.startsWith("text/") == true) -> FileCategory.DOCUMENT
                ext in listOf("mp3", "wav", "ogg", "m4a", "flac") || (mimeType?.startsWith("audio/") == true) -> FileCategory.AUDIO
                ext in listOf("mp4", "mkv", "webm", "avi", "mov") || (mimeType?.startsWith("video/") == true) -> FileCategory.VIDEO
                else -> FileCategory.UNKNOWN
            }
        }

        fun formatBytes(bytes: Long, isDirectory: Boolean): String {
            if (isDirectory) return "Folder"
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
            val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
        }

        fun formatDate(millis: Long): String {
            if (millis <= 0) return "-"
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }
}

data class FileMetadata(
    val entry: FileEntry,
    val formattedSize: String,
    val formattedDate: String,
    val childCount: Int? = null,
    val md5Hash: String? = null,
    val sha256Hash: String? = null,
    val absolutePathOrUri: String
)

enum class SortField { NAME, SIZE, DATE, TYPE }
enum class SortDirection { ASCENDING, DESCENDING }

data class SortOrder(
    val field: SortField = SortField.NAME,
    val direction: SortDirection = SortDirection.ASCENDING
)
