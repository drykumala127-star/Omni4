package com.omnifile.feature.toolkit.system

import org.json.JSONObject
import java.io.File

data class CrashRecoveryCheckpoint(
    val projectId: String,
    val activeFilePath: String,
    val unsavedContent: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

class CrashRecoveryManager(private val recoveryDir: File) {

    init {
        recoveryDir.mkdirs()
    }

    fun saveCheckpoint(checkpoint: CrashRecoveryCheckpoint) {
        val file = File(recoveryDir, "recovery_${checkpoint.projectId}.json")
        val json = JSONObject().apply {
            put("projectId", checkpoint.projectId)
            put("activeFilePath", checkpoint.activeFilePath)
            put("unsavedContent", checkpoint.unsavedContent)
            put("timestampMillis", checkpoint.timestampMillis)
        }
        file.writeText(json.toString(2))
    }

    fun loadCheckpoint(projectId: String): CrashRecoveryCheckpoint? {
        val file = File(recoveryDir, "recovery_$projectId.json")
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            CrashRecoveryCheckpoint(
                projectId = json.getString("projectId"),
                activeFilePath = json.getString("activeFilePath"),
                unsavedContent = json.getString("unsavedContent"),
                timestampMillis = json.optLong("timestampMillis")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun clearCheckpoint(projectId: String) {
        val file = File(recoveryDir, "recovery_$projectId.json")
        if (file.exists()) file.delete()
    }
}
