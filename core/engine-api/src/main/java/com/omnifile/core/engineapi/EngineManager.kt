package com.omnifile.core.engineapi

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Central orchestrator managing engine lifecycles, execution requests, and logging.
 */
class EngineManager private constructor() {

    private val engines = linkedMapOf<String, ToolEngine>()

    fun register(engine: ToolEngine) {
        engines[engine.id] = engine
    }

    fun get(id: String): ToolEngine? = engines[id]

    fun all(): List<ToolEngine> = engines.values.toList()

    suspend fun execute(engineId: String, request: EngineRequest): EngineResult = withContext(Dispatchers.IO) {
        val engine = engines[engineId]
            ?: return@withContext EngineResult(
                success = false,
                message = "Engine '$engineId' tidak ditemukan di Registry"
            )
        val startTime = System.currentTimeMillis()
        try {
            val result = engine.execute(request)
            val duration = System.currentTimeMillis() - startTime
            result.copy(executionTimeMillis = duration)
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            EngineResult(
                success = false,
                message = "Eksekusi engine '$engineId' gagal: ${e.message}",
                executionTimeMillis = duration,
                details = mapOf("exception" to (e.javaClass.simpleName))
            )
        }
    }

    companion object {
        val instance: EngineManager by lazy { EngineManager() }
    }
}
