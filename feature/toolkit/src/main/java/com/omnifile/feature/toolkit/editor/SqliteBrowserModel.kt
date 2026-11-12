package com.omnifile.feature.toolkit.editor

import java.io.File

data class SqliteTableInfo(
    val name: String,
    val sql: String,
    val rowCount: Int = 0
)

data class SqliteQueryResult(
    val columns: List<String>,
    val rows: List<List<String>>,
    val executionTimeMillis: Long
)

object SqliteBrowserModel {

    val SQLITE_HEADER_MAGIC = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    fun isSqliteDatabase(file: File): Boolean {
        if (!file.exists() || file.length() < 16) return false
        val header = ByteArray(16)
        file.inputStream().use { it.read(header) }
        return header.contentEquals(SQLITE_HEADER_MAGIC)
    }

    fun parseTableNamesFromSchema(schemaSql: String): List<String> {
        val regex = Regex("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?[\"`\\[]?(\\w+)[\"`\\]]?", RegexOption.IGNORE_CASE)
        return regex.findAll(schemaSql).map { it.groupValues[1] }.toList()
    }
}
