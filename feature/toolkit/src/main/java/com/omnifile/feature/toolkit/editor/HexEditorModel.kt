package com.omnifile.feature.toolkit.editor

data class HexLine(
    val offset: Int,
    val hexString: String,
    val asciiString: String
)

object HexEditorModel {

    fun generateHexDump(data: ByteArray, maxBytes: Int = 4096): List<HexLine> {
        val lines = mutableListOf<HexLine>()
        val limit = minOf(data.size, maxBytes)

        for (i in 0 until limit step 16) {
            val chunkLength = minOf(16, limit - i)
            val hexBuilder = StringBuilder()
            val asciiBuilder = StringBuilder()

            for (j in 0 until 16) {
                if (j < chunkLength) {
                    val b = data[i + j].toInt() and 0xFF
                    hexBuilder.append("%02X ".format(b))
                    val c = b.toChar()
                    if (c in ' '..'~') {
                        asciiBuilder.append(c)
                    } else {
                        asciiBuilder.append('.')
                    }
                } else {
                    hexBuilder.append("   ")
                }
                if (j == 7) hexBuilder.append(" ")
            }

            lines.add(
                HexLine(
                    offset = i,
                    hexString = hexBuilder.toString().trimEnd(),
                    asciiString = asciiBuilder.toString()
                )
            )
        }
        return lines
    }

    fun modifyByte(data: ByteArray, offset: Int, newByte: Byte): ByteArray {
        if (offset in data.indices) {
            val copy = data.clone()
            copy[offset] = newByte
            return copy
        }
        return data
    }
}
