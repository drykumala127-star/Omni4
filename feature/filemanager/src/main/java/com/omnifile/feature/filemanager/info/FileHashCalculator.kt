package com.omnifile.feature.filemanager.info

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest

/**
 * Result data class containing cryptographic hashes for a file.
 */
data class FileHashes(
    val md5: String,
    val sha1: String,
    val sha256: String
)

/**
 * High-performance streaming hash calculator.
 * Computes MD5, SHA-1, and SHA-256 in a single I/O pass using a 64KB chunk buffer to prevent OOM.
 */
object FileHashCalculator {

    suspend fun computeHashes(contentResolver: ContentResolver, uri: Uri): Result<FileHashes> =
        withContext(Dispatchers.IO) {
            runCatching {
                val md5Digest = MessageDigest.getInstance("MD5")
                val sha1Digest = MessageDigest.getInstance("SHA-1")
                val sha256Digest = MessageDigest.getInstance("SHA-256")

                val inputStream: InputStream = contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Tidak dapat membuka stream untuk URI: $uri")

                inputStream.use { input ->
                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        md5Digest.update(buffer, 0, bytesRead)
                        sha1Digest.update(buffer, 0, bytesRead)
                        sha256Digest.update(buffer, 0, bytesRead)
                    }
                }

                FileHashes(
                    md5 = bytesToHex(md5Digest.digest()),
                    sha1 = bytesToHex(sha1Digest.digest()),
                    sha256 = bytesToHex(sha256Digest.digest())
                )
            }
        }

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt() and 0xFF
            result.append(hexChars[i shr 4])
            result.append(hexChars[i and 0x0F])
        }
        return result.toString()
    }
}
