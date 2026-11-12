package com.omnifile.core.workspace

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * Manages original APK preservation, integrity verification, and tamper detection.
 */
class OriginalApkManager(private val projectDir: File) {

    val originalDir: File get() = File(projectDir, "original")
    val originalApkFile: File get() = File(originalDir, "original.apk")

    /**
     * Stores the imported APK into the project's original/ directory, computes its SHA-256,
     * and sets file permissions to read-only for tamper protection.
     */
    fun storeOriginalApk(sourceApkFile: File): String {
        originalDir.mkdirs()
        sourceApkFile.copyTo(originalApkFile, overwrite = true)
        // Make original APK read-only to prevent accidental modification
        originalApkFile.setReadOnly()

        val sha256 = computeFileSha256(originalApkFile)
        return sha256
    }

    /**
     * Verifies that the original APK has not been tampered with or corrupted.
     */
    fun verifyIntegrity(expectedSha256: String): Boolean {
        if (!originalApkFile.exists() || !originalApkFile.isFile) return false
        val currentSha256 = computeFileSha256(originalApkFile)
        return currentSha256.equals(expectedSha256, ignoreCase = true)
    }

    companion object {
        fun computeFileSha256(file: File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(64 * 1024)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
