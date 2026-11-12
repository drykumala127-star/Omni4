package com.omnifile.feature.toolkit.system

data class BatchRenameRule(
    val prefix: String = "",
    val suffix: String = "",
    val findText: String = "",
    val replaceWith: String = "",
    val startNumberIndex: Int? = null
)

object BatchProcessor {

    fun generateNewNames(filenames: List<String>, rule: BatchRenameRule): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        var counter = rule.startNumberIndex ?: 1

        for (name in filenames) {
            val dot = name.lastIndexOf('.')
            val base = if (dot > 0) name.substring(0, dot) else name
            val ext = if (dot > 0) name.substring(dot) else ""

            var newBase = base
            if (rule.findText.isNotEmpty()) {
                newBase = newBase.replace(rule.findText, rule.replaceWith)
            }
            if (rule.prefix.isNotEmpty()) {
                newBase = rule.prefix + newBase
            }
            if (rule.suffix.isNotEmpty()) {
                newBase = newBase + rule.suffix
            }
            if (rule.startNumberIndex != null) {
                newBase = "${newBase}_$counter"
                counter++
            }
            val finalName = "$newBase$ext"
            result.add(Pair(name, finalName))
        }
        return result
    }
}
