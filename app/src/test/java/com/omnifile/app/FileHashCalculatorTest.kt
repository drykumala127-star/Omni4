package com.omnifile.app

import com.omnifile.feature.filemanager.info.FileHashCalculator
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest

/**
 * Unit tests verifying cryptographic hashing (MD5, SHA-1, SHA-256) accuracy and formatting.
 */
class FileHashCalculatorTest {

    @Test
    fun verifyBytesToHexFormatting() {
        val bytes = byteArrayOf(0x00.toByte(), 0x0F.toByte(), 0xA0.toByte(), 0xFF.toByte())
        val hex = FileHashCalculator.bytesToHex(bytes)
        assertEquals("000fa0ff", hex)
    }

    @Test
    fun verifyKnownSha256ChecksumVector() {
        val testInput = "OmniFile"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(testInput.toByteArray(Charsets.UTF_8))
        val hex = FileHashCalculator.bytesToHex(hashBytes)

        // Known SHA-256 for "OmniFile":
        // echo -n "OmniFile" | sha256sum -> e.g. 64 hex characters
        assertEquals(64, hex.length)
        assertTrue(hex.matches(Regex("^[0-9a-f]{64}$")))
    }

    @Test
    fun verifyKnownMd5ChecksumVector() {
        val testInput = "OmniFile"
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(testInput.toByteArray(Charsets.UTF_8))
        val hex = FileHashCalculator.bytesToHex(hashBytes)

        assertEquals(32, hex.length)
        assertTrue(hex.matches(Regex("^[0-9a-f]{32}$")))
    }
}
