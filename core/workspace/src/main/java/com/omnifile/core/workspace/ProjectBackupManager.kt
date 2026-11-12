package com.omnifile.core.workspace

import com.omnifile.core.workspace.model.ProjectArea
import com.omnifile.core.workspace.model.ProjectBackupManifest
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Manages atomic project workspace snapshots, backup archive generation, and restoration.
 */
class ProjectBackupManager(private val projectDir: File) {

    val backupsDir: File get() = File(projectDir, ProjectArea.BACKUPS.folderName)

    /**
     * Creates a ZIP backup of the workspace (excluding backups/ directory itself to avoid recursion).
     */
    fun createSnapshot(projectId: String, projectName: String): ProjectBackupManifest {
        backupsDir.mkdirs()
        val timestamp = System.currentTimeMillis()
        val backupId = "backup-$timestamp"
        val zipFile = File(backupsDir, "$backupId.zip")

        val includedAreas = listOf(
            ProjectArea.ORIGINAL,
            ProjectArea.DECODED,
            ProjectArea.DEX,
            ProjectArea.SOURCE,
            ProjectArea.MODIFIED
        )

        var totalFiles = 0
        var totalBytes = 0L

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            for (area in includedAreas) {
                val areaDir = File(projectDir, area.folderName)
                if (areaDir.exists() && areaDir.isDirectory) {
                    areaDir.walkTopDown().forEach { file ->
                        if (file.isFile) {
                            val relPath = file.relativeTo(projectDir).path.replace('\\', '/')
                            val entry = ZipEntry(relPath)
                            entry.time = file.lastModified()
                            zos.putNextEntry(entry)
                            FileInputStream(file).use { fis ->
                                val buffer = ByteArray(32 * 1024)
                                var read: Int
                                while (fis.read(buffer).also { read = it } != -1) {
                                    zos.write(buffer, 0, read)
                                    totalBytes += read
                                }
                            }
                            zos.closeEntry()
                            totalFiles++
                        }
                    }
                }
            }
            // Include project.json
            val projectJson = File(projectDir, "project.json")
            if (projectJson.exists()) {
                val entry = ZipEntry("project.json")
                zos.putNextEntry(entry)
                FileInputStream(projectJson).use { it.copyTo(zos) }
                zos.closeEntry()
                totalFiles++
            }
        }

        return ProjectBackupManifest(
            backupId = backupId,
            projectId = projectId,
            projectName = projectName,
            timestampMillis = timestamp,
            includedAreas = includedAreas,
            fileCount = totalFiles,
            totalSizeBytes = zipFile.length()
        )
    }

    /**
     * Restores project state from a backup ZIP.
     */
    fun restoreSnapshot(backupZipFile: File) {
        if (!backupZipFile.exists() || !backupZipFile.isFile) {
            throw IllegalArgumentException("Berkas backup tidak ditemukan: ${backupZipFile.path}")
        }

        ZipInputStream(BufferedInputStream(FileInputStream(backupZipFile))).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name
                val targetFile = File(projectDir, name)
                WorkspaceSecurityValidator.assertWithinWorkspace(projectDir, targetFile)

                if (entry.isDirectory) {
                    targetFile.mkdirs()
                } else {
                    targetFile.parentFile?.mkdirs()
                    FileOutputStream(targetFile).use { fos ->
                        val buffer = ByteArray(32 * 1024)
                        var read: Int
                        while (zis.read(buffer).also { read = it } != -1) {
                            fos.write(buffer, 0, read)
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    /**
     * Lists all available backup snapshot files.
     */
    fun listSnapshots(): List<File> {
        if (!backupsDir.exists()) return emptyList()
        return backupsDir.listFiles { file -> file.isFile && file.name.endsWith(".zip") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
