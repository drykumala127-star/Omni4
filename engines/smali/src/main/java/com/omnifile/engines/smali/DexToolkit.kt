package com.omnifile.engines.smali

import java.io.File
import java.security.MessageDigest
import java.util.zip.Adler32

/**
 * Parsed DEX header model.
 */
data class DexHeader(
    val magic: String,
    val checksum: Long,
    val sha1Signature: String,
    val fileSize: Int,
    val headerSize: Int,
    val endianTag: Long,
    val stringIdsSize: Int,
    val typeIdsSize: Int,
    val protoIdsSize: Int,
    val fieldIdsSize: Int,
    val methodIdsSize: Int,
    val classDefsSize: Int,
    val dataSize: Int,
    val isValidMagic: Boolean,
    val isValidChecksum: Boolean,
    val isValidSignature: Boolean
)

/**
 * Low-level Android Dalvik Executable (DEX) binary parser and validator.
 */
object DexToolkit {

    fun parseHeader(dexFile: File): DexHeader {
        val bytes = dexFile.readBytes()
        if (bytes.size < 0x70) {
            throw IllegalArgumentException("DEX file size is smaller than standard header (112 bytes)")
        }

        val magic = String(bytes, 0, 8, Charsets.US_ASCII).replace("\n", "\\n").replace("\u0000", "\\0")
        val isValidMagic = bytes[0] == 'd'.code.toByte() &&
                bytes[1] == 'e'.code.toByte() &&
                bytes[2] == 'x'.code.toByte() &&
                bytes[3] == '\n'.code.toByte()

        val expectedChecksum = readUInt(bytes, 8)
        val expectedSha1 = bytes.copyOfRange(12, 32).joinToString("") { "%02x".format(it) }

        // Verify Checksum (Adler32 from offset 12 to EOF)
        val adler = Adler32()
        adler.update(bytes, 12, bytes.size - 12)
        val actualChecksum = adler.value
        val isValidChecksum = actualChecksum == expectedChecksum

        // Verify SHA-1 signature (from offset 32 to EOF)
        val md = MessageDigest.getInstance("SHA-1")
        md.update(bytes, 32, bytes.size - 32)
        val actualSha1 = md.digest().joinToString("") { "%02x".format(it) }
        val isValidSignature = actualSha1.equals(expectedSha1, ignoreCase = true)

        val fileSize = readInt(bytes, 0x20)
        val headerSize = readInt(bytes, 0x24)
        val endianTag = readUInt(bytes, 0x28)
        val stringIdsSize = readInt(bytes, 0x38)
        val typeIdsSize = readInt(bytes, 0x40)
        val protoIdsSize = readInt(bytes, 0x48)
        val fieldIdsSize = readInt(bytes, 0x50)
        val methodIdsSize = readInt(bytes, 0x58)
        val classDefsSize = readInt(bytes, 0x60)
        val dataSize = readInt(bytes, 0x68)

        return DexHeader(
            magic = magic,
            checksum = expectedChecksum,
            sha1Signature = expectedSha1,
            fileSize = fileSize,
            headerSize = headerSize,
            endianTag = endianTag,
            stringIdsSize = stringIdsSize,
            typeIdsSize = typeIdsSize,
            protoIdsSize = protoIdsSize,
            fieldIdsSize = fieldIdsSize,
            methodIdsSize = methodIdsSize,
            classDefsSize = classDefsSize,
            dataSize = dataSize,
            isValidMagic = isValidMagic,
            isValidChecksum = isValidChecksum,
            isValidSignature = isValidSignature
        )
    }

    private fun readInt(b: ByteArray, offset: Int): Int {
        return (b[offset].toInt() and 0xFF) or
                ((b[offset + 1].toInt() and 0xFF) shl 8) or
                ((b[offset + 2].toInt() and 0xFF) shl 16) or
                ((b[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun readUInt(b: ByteArray, offset: Int): Long {
        return readInt(b, offset).toLong() and 0xFFFFFFFFL
    }
}
