package com.omnifile.app

import com.omnifile.feature.filemanager.model.ZipSecurityException
import com.omnifile.feature.filemanager.zip.ZipSecurityValidator
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Security audit tests verifying Zip Slip directory traversal defenses and Zip Bomb mitigations.
 */
class ZipSecurityTest {

    @Test
    fun verifyLegitimateZipEntryNamesAllowed() {
        val legitimate = listOf(
            "classes.dex",
            "assets/config.json",
            "res/drawable-xxhdpi/icon.png",
            "lib/arm64-v8a/libnative.so"
        )
        for (name in legitimate) {
            val sanitized = ZipSecurityValidator.validateAndSanitizeEntryName(name)
            assertEquals(name, sanitized)
        }
    }

    @Test
    fun verifyZipSlipPathTraversalBlocked() {
        val dangerousEntries = listOf(
            "../evil.sh",
            "../../etc/passwd",
            "assets/../../malicious.so",
            "res/../..\\windows/system32/cmd.exe",
            "..\\..\\payload.apk",
            "/absolute/path/file.txt"
        )

        for (entry in dangerousEntries) {
            try {
                ZipSecurityValidator.validateAndSanitizeEntryName(entry)
                fail("Expected ZipSecurityException for dangerous entry: $entry")
            } catch (e: ZipSecurityException) {
                assertTrue(e.violationReason.contains("Zip Slip") || e.violationReason.contains("traversal"))
            }
        }
    }

    @Test
    fun verifyPoisonedNullByteBlocked() {
        val nullByteEntry = "legit.txt\u0000.exe"
        try {
            ZipSecurityValidator.validateAndSanitizeEntryName(nullByteEntry)
            fail("Expected ZipSecurityException for poisoned null byte entry")
        } catch (e: ZipSecurityException) {
            assertTrue(e.violationReason.contains("null byte"))
        }
    }

    @Test
    fun verifyCanonicalPathContainmentAsserted() {
        val tempDest = File("/tmp/omnifile_test_dest")
        val legitimateTarget = File(tempDest, "subfolder/extracted.txt")
        // Legitimate target must pass without exception
        ZipSecurityValidator.assertInsideDestination(legitimateTarget, tempDest)

        val maliciousTarget = File(tempDest, "../outside.txt")
        try {
            ZipSecurityValidator.assertInsideDestination(maliciousTarget, tempDest)
            fail("Expected ZipSecurityException when target escapes destination root")
        } catch (e: ZipSecurityException) {
            assertTrue(e.violationReason.contains("Zip Slip blocked"))
        }
    }

    @Test
    fun verifyZipBombDefenseThresholds() {
        // Exceeding entry count
        try {
            ZipSecurityValidator.checkZipBombProtection(
                entriesCount = 10_001,
                totalUncompressedBytes = 100_000L,
                totalCompressedBytes = 10_000L,
                maxEntries = 10_000
            )
            fail("Expected ZipSecurityException for exceeding max entry count")
        } catch (e: ZipSecurityException) {
            assertTrue(e.violationReason.contains("entry count"))
        }

        // Exceeding uncompressed size
        try {
            ZipSecurityValidator.checkZipBombProtection(
                entriesCount = 50,
                totalUncompressedBytes = 3L * 1024L * 1024L * 1024L, // 3 GB
                totalCompressedBytes = 10_000L,
                maxUncompressedBytes = 2L * 1024L * 1024L * 1024L
            )
            fail("Expected ZipSecurityException for exceeding max uncompressed size")
        } catch (e: ZipSecurityException) {
            assertTrue(e.violationReason.contains("uncompressed size"))
        }
    }
}
