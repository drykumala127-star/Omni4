package com.omnifile.feature.toolkit.editor

import java.io.File

data class CodeSearchResult(
    val file: File,
    val lineNumber: Int,
    val lineContent: String,
    val matchStart: Int,
    val matchEnd: Int
)

object CodeSearchEngine {

    fun searchDirectory(
        directory: File,
        query: String,
        isRegex: Boolean = false,
        caseSensitive: Boolean = false,
        maxResults: Int = 500
    ): List<CodeSearchResult> {
        if (!directory.exists() || !directory.isDirectory) return emptyList()

        val results = mutableListOf<CodeSearchResult>()
        val regex = try {
            if (isRegex) {
                if (caseSensitive) Regex(query) else Regex(query, RegexOption.IGNORE_CASE)
            } else {
                if (caseSensitive) Regex(Regex.escape(query)) else Regex(Regex.escape(query), RegexOption.IGNORE_CASE)
            }
        } catch (_: Exception) {
            return emptyList()
        }

        directory.walkTopDown().forEach { file ->
            if (file.isFile && isSearchableExtension(file.extension)) {
                try {
                    file.useLines { lines ->
                        var lineNum = 1
                        for (line in lines) {
                            val matches = regex.findAll(line)
                            for (m in matches) {
                                results.add(
                                    CodeSearchResult(
                                        file = file,
                                        lineNumber = lineNum,
                                        lineContent = line.trim(),
                                        matchStart = m.range.first,
                                        matchEnd = m.range.last + 1
                                    )
                                )
                                if (results.size >= maxResults) return results
                            }
                            lineNum++
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        return results
    }

    private fun isSearchableExtension(ext: String): Boolean {
        return ext.lowercase() in listOf(
            "java", "kt", "smali", "xml", "json", "gradle", "kts", "txt", "properties", "cpp", "c", "h"
        )
    }
}
