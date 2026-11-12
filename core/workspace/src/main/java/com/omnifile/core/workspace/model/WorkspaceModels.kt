package com.omnifile.core.workspace.model

import java.io.File

/**
 * Standard project areas inside a modular OmniFile APK project workspace.
 */
enum class ProjectArea(val folderName: String) {
    ORIGINAL("original"),     // Read-only original APK
    DECODED("decoded"),       // APKTool decompiled resources and smali
    DEX("dex"),               // Extracted classes*.dex binaries
    SOURCE("source"),         // JADX decompiled Java source tree
    MODIFIED("modified"),     // User modified resources / smali
    BUILD("build"),           // Assembly staging & unsigned build output
    SIGNED("signed"),         // Final aligned & signed APK
    BACKUPS("backups"),       // Workspace project state snapshots
    TEMP("temp")              // Intermediate working files
}

/**
 * Manifest metadata describing an imported APK development project.
 */
data class ProjectMetadata(
    val id: String,
    val name: String,
    val packageName: String = "",
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val minSdk: Int = 26,
    val targetSdk: Int = 34,
    val originalApkSha256: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val lastModifiedMillis: Long = System.currentTimeMillis(),
    val isDecompiled: Boolean = false,
    val isModified: Boolean = false
)

/**
 * Audit log entry for workspace modifications and operations.
 */
data class ProjectHistoryEntry(
    val id: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val action: String,
    val details: String = "",
    val area: ProjectArea? = null
)

/**
 * Snapshot descriptor for workspace backup and restore.
 */
data class ProjectBackupManifest(
    val backupId: String,
    val projectId: String,
    val projectName: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val includedAreas: List<ProjectArea>,
    val fileCount: Int,
    val totalSizeBytes: Long
)
