package com.omnifile.feature.filemanager.model

import android.net.Uri

/**
 * Persisted favorite/pinned file or folder bookmark.
 */
data class BookmarkItem(
    val uriString: String,
    val name: String,
    val isDirectory: Boolean,
    val addedTimeMillis: Long = System.currentTimeMillis()
) {
    val uri: Uri get() = Uri.parse(uriString)
}

/**
 * Access history entry for recent files view.
 */
data class RecentAccessRecord(
    val uriString: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val accessTimeMillis: Long = System.currentTimeMillis()
) {
    val uri: Uri get() = Uri.parse(uriString)
}
