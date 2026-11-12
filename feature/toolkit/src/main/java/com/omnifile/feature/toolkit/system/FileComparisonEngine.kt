package com.omnifile.feature.toolkit.system

enum class DiffLineType { UNCHANGED, ADDED, REMOVED, MODIFIED }

data class DiffLine(
    val type: DiffLineType,
    val oldLineNumber: Int?,
    val newLineNumber: Int?,
    val content: String
)

data class ComparisonResult(
    val areIdentical: Boolean,
    val additionsCount: Int,
    val deletionsCount: Int,
    val diffLines: List<DiffLine>
)

object FileComparisonEngine {

    fun compareTexts(originalText: String, modifiedText: String): ComparisonResult {
        if (originalText == modifiedText) {
            val lines = originalText.lines().mapIndexed { i, line ->
                DiffLine(DiffLineType.UNCHANGED, i + 1, i + 1, line)
            }
            return ComparisonResult(areIdentical = true, additionsCount = 0, deletionsCount = 0, diffLines = lines)
        }

        val oldLines = originalText.lines()
        val newLines = modifiedText.lines()
        val diffList = mutableListOf<DiffLine>()

        var additions = 0
        var deletions = 0

        val maxLen = maxOf(oldLines.size, newLines.size)
        var oldIdx = 0
        var newIdx = 0

        while (oldIdx < oldLines.size || newIdx < newLines.size) {
            val oldLine = oldLines.getOrNull(oldIdx)
            val newLine = newLines.getOrNull(newIdx)

            if (oldLine == newLine && oldLine != null) {
                diffList.add(DiffLine(DiffLineType.UNCHANGED, oldIdx + 1, newIdx + 1, oldLine))
                oldIdx++
                newIdx++
            } else if (oldLine != null && (newLine == null || !newLines.contains(oldLine))) {
                diffList.add(DiffLine(DiffLineType.REMOVED, oldIdx + 1, null, oldLine))
                deletions++
                oldIdx++
            } else if (newLine != null) {
                diffList.add(DiffLine(DiffLineType.ADDED, null, newIdx + 1, newLine))
                additions++
                newIdx++
            }
        }

        return ComparisonResult(
            areIdentical = additions == 0 && deletions == 0,
            additionsCount = additions,
            deletionsCount = deletions,
            diffLines = diffList
        )
    }
}
