package com.omnifile.core.engineapi

import android.content.Context
import java.io.File

enum class EngineType {
    APKTOOL,
    JADX,
    SMALI,
    DEX_TOOLKIT,
    APK_ANALYZER,
    SIGNER
}

data class EngineRequest(
    val inputPath: String,
    val outputPath: String? = null,
    val operation: String,
    val options: Map<String, String> = emptyMap(),
    val onLog: ((String) -> Unit)? = null
)

data class EngineProgress(
    val engineType: EngineType,
    val step: String,
    val percent: Int = 0,
    val isIndeterminate: Boolean = false
)

data class EngineResult(
    val success: Boolean,
    val outputPath: String? = null,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val executionTimeMillis: Long = 0L,
    val logs: List<String> = emptyList()
)

interface ToolEngine {
    val id: String
    val displayName: String
    val version: String
    val engineType: EngineType
    suspend fun initialize(context: Context)
    suspend fun execute(request: EngineRequest): EngineResult
    suspend fun shutdown()
}
