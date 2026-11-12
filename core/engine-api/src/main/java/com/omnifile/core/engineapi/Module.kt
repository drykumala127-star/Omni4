package com.omnifile.core.engineapi

class EngineRegistry {
    private val engines = linkedMapOf<String, ToolEngine>()
    fun register(engine: ToolEngine) { engines[engine.id] = engine }
    fun get(id: String): ToolEngine? = engines[id]
    fun all(): List<ToolEngine> = engines.values.toList()
}

