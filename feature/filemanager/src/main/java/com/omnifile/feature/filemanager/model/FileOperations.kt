package com.omnifile.feature.filemanager.model

/**
 * Supported file operation types.
 */
enum class FileOperationType {
    COPY,
    MOVE,
    RENAME,
    DELETE,
    CREATE_FOLDER,
    COMPRESS_ZIP,
    EXTRACT_ZIP
}

/**
 * Strategy when destination file already exists.
 */
enum class ConflictStrategy {
    OVERWRITE,
    AUTO_RENAME,
    CANCEL
}

/**
 * Operational progress model for background I/O operations.
 */
data class OperationProgress(
    val operation: FileOperationType,
    val currentItemName: String = "",
    val itemsProcessed: Int = 0,
    val totalItems: Int = 0,
    val bytesProcessed: Long = 0L,
    val totalBytes: Long = 0L,
    val isIndeterminate: Boolean = false,
    val isCancellable: Boolean = true
) {
    val progressFraction: Float
        get() = when {
            totalBytes > 0L -> (bytesProcessed.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
            totalItems > 0 -> (itemsProcessed.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f)
            else -> 0f
        }
}

/**
 * Result wrapper for file operations.
 */
sealed class FileOperationResult {
    data class Success(val affectedCount: Int, val message: String) : FileOperationResult()
    data class PartialSuccess(val successCount: Int, val failedCount: Int, val errors: List<String>) : FileOperationResult()
    data class Failure(val errorMessage: String, val throwable: Throwable? = null) : FileOperationResult()
    data object Cancelled : FileOperationResult()
}

/**
 * Specific exception thrown when a security threat (Zip Slip / Zip Bomb) is detected.
 */
class ZipSecurityException(val violationReason: String) : SecurityException(violationReason)
