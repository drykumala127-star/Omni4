package com.omnifile.core.workspace

import java.io.File

/**
 * Security auditor enforcing strict sandbox containment for project workspace files.
 */
object WorkspaceSecurityValidator {

    /**
     * Asserts that targetFile resides strictly within projectRoot.
     * Prevents directory traversal attacks escaping the project sandbox.
     */
    fun assertWithinWorkspace(projectRoot: File, targetFile: File) {
        val rootCanonical = projectRoot.canonicalPath
        val targetCanonical = targetFile.canonicalPath

        val prefixWithSep = if (rootCanonical.endsWith(File.separator)) {
            rootCanonical
        } else {
            rootCanonical + File.separator
        }

        if (targetCanonical != rootCanonical && !targetCanonical.startsWith(prefixWithSep)) {
            throw SecurityException(
                "Sandbox Violation! File path '$targetCanonical' escapes project root '$rootCanonical'"
            )
        }
    }

    /**
     * Sanitizes file/folder names within the project workspace.
     */
    fun sanitizeProjectName(name: String): String {
        return name.replace(Regex("[/\\\\:*?\"<>|]"), "_").trim()
    }
}
