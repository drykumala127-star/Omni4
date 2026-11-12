package com.omnifile.core.workspace

import com.omnifile.core.workspace.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Core Workspace Manager orchestrating APK projects, area folders, metadata, and history.
 */
class WorkspaceManager(val baseWorkspacesDir: File) {

    init {
        baseWorkspacesDir.mkdirs()
    }

    /**
     * Initializes a new workspace project directory structure.
     */
    fun createProject(projectName: String, sourceApkFile: File? = null): ProjectMetadata {
        val sanitized = WorkspaceSecurityValidator.sanitizeProjectName(projectName)
        val projectId = "proj_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val projectDir = File(baseWorkspacesDir, "$sanitized-$projectId")
        projectDir.mkdirs()

        // Create standard project area folders
        for (area in ProjectArea.values()) {
            File(projectDir, area.folderName).mkdirs()
        }

        var apkSha256 = ""
        if (sourceApkFile != null && sourceApkFile.exists()) {
            val originalManager = OriginalApkManager(projectDir)
            apkSha256 = originalManager.storeOriginalApk(sourceApkFile)
        }

        val metadata = ProjectMetadata(
            id = projectId,
            name = projectName,
            originalApkSha256 = apkSha256,
            createdAtMillis = System.currentTimeMillis(),
            lastModifiedMillis = System.currentTimeMillis()
        )

        saveProjectMetadata(projectDir, metadata)
        recordHistory(projectDir, "Project Created", "Workspace initialized for '$projectName'")

        return metadata
    }

    /**
     * Obtains the root directory of a project by its directory name or metadata ID.
     */
    fun getProjectDirectory(projectFolder: String): File {
        val dir = File(baseWorkspacesDir, projectFolder)
        WorkspaceSecurityValidator.assertWithinWorkspace(baseWorkspacesDir, dir)
        return dir
    }

    /**
     * Obtains the directory for a specific ProjectArea within the project.
     */
    fun getAreaDirectory(projectDir: File, area: ProjectArea): File {
        val dir = File(projectDir, area.folderName)
        WorkspaceSecurityValidator.assertWithinWorkspace(projectDir, dir)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Loads the ProjectMetadata manifest from project.json.
     */
    fun loadProjectMetadata(projectDir: File): ProjectMetadata? {
        val jsonFile = File(projectDir, "project.json")
        if (!jsonFile.exists()) return null
        return try {
            val json = JSONObject(jsonFile.readText())
            ProjectMetadata(
                id = json.getString("id"),
                name = json.getString("name"),
                packageName = json.optString("packageName", ""),
                versionCode = json.optInt("versionCode", 1),
                versionName = json.optString("versionName", "1.0.0"),
                minSdk = json.optInt("minSdk", 26),
                targetSdk = json.optInt("targetSdk", 34),
                originalApkSha256 = json.optString("originalApkSha256", ""),
                createdAtMillis = json.optLong("createdAtMillis", System.currentTimeMillis()),
                lastModifiedMillis = json.optLong("lastModifiedMillis", System.currentTimeMillis()),
                isDecompiled = json.optBoolean("isDecompiled", false),
                isModified = json.optBoolean("isModified", false)
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Saves the ProjectMetadata manifest to project.json.
     */
    fun saveProjectMetadata(projectDir: File, metadata: ProjectMetadata) {
        val jsonFile = File(projectDir, "project.json")
        val json = JSONObject().apply {
            put("id", metadata.id)
            put("name", metadata.name)
            put("packageName", metadata.packageName)
            put("versionCode", metadata.versionCode)
            put("versionName", metadata.versionName)
            put("minSdk", metadata.minSdk)
            put("targetSdk", metadata.targetSdk)
            put("originalApkSha256", metadata.originalApkSha256)
            put("createdAtMillis", metadata.createdAtMillis)
            put("lastModifiedMillis", System.currentTimeMillis())
            put("isDecompiled", metadata.isDecompiled)
            put("isModified", metadata.isModified)
        }
        jsonFile.writeText(json.toString(2))
    }

    /**
     * Logs an operation in history.json for revision history and crash recovery.
     */
    fun recordHistory(projectDir: File, action: String, details: String, area: ProjectArea? = null) {
        val historyFile = File(projectDir, "history.json")
        val array = if (historyFile.exists()) {
            try { JSONArray(historyFile.readText()) } catch (_: Exception) { JSONArray() }
        } else {
            JSONArray()
        }

        val entry = JSONObject().apply {
            put("id", UUID.randomUUID().toString())
            put("timestampMillis", System.currentTimeMillis())
            put("action", action)
            put("details", details)
            put("area", area?.name)
        }
        array.put(entry)
        historyFile.writeText(array.toString(2))
    }

    /**
     * Lists all projects currently resident in the workspace directory.
     */
    fun listProjects(): List<ProjectMetadata> {
        val dirs = baseWorkspacesDir.listFiles { file -> file.isDirectory } ?: return emptyList()
        return dirs.mapNotNull { loadProjectMetadata(it) }.sortedByDescending { it.lastModifiedMillis }
    }
}
