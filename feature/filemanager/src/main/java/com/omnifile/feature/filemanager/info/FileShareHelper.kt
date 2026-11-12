package com.omnifile.feature.filemanager.info

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Helper to create system Intents for sharing files and opening files with external apps.
 */
object FileShareHelper {

    /**
     * Creates an Intent to share a single file with external apps.
     */
    fun createShareIntent(uri: Uri, mimeType: String?, title: String): Intent {
        val type = if (mimeType.isNullOrBlank()) "application/octet-stream" else mimeType
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            this.type = type
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, "Bagikan $title")
    }

    /**
     * Creates an Intent to share multiple selected files simultaneously.
     */
    fun createShareMultipleIntent(uris: List<Uri>, title: String = "Bagikan Berkas"): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, title)
    }

    /**
     * Creates an Intent to open a file with an appropriate viewer/editor application.
     */
    fun createOpenFileIntent(uri: Uri, mimeType: String?): Intent {
        val type = if (mimeType.isNullOrBlank()) "*/*" else mimeType
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
