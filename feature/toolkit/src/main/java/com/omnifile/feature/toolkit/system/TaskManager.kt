package com.omnifile.feature.toolkit.system

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class TaskStatus { PENDING, RUNNING, COMPLETED, FAILED, CANCELLED }

data class ManagedTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val progressPercent: Int = 0,
    val status: TaskStatus = TaskStatus.PENDING,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = null,
    val errorMessage: String? = null
)

class TaskManager private constructor() {

    private val _tasksFlow = MutableStateFlow<List<ManagedTask>>(emptyList())
    val tasksFlow: StateFlow<List<ManagedTask>> = _tasksFlow.asStateFlow()

    fun addTask(title: String): ManagedTask {
        val task = ManagedTask(title = title, status = TaskStatus.RUNNING)
        _tasksFlow.value = listOf(task) + _tasksFlow.value
        return task
    }

    fun updateProgress(taskId: String, percent: Int) {
        _tasksFlow.value = _tasksFlow.value.map {
            if (it.id == taskId) it.copy(progressPercent = percent.coerceIn(0, 100)) else it
        }
    }

    fun markComplete(taskId: String) {
        _tasksFlow.value = _tasksFlow.value.map {
            if (it.id == taskId) it.copy(
                status = TaskStatus.COMPLETED,
                progressPercent = 100,
                completedAtMillis = System.currentTimeMillis()
            ) else it
        }
    }

    fun markFailed(taskId: String, error: String) {
        _tasksFlow.value = _tasksFlow.value.map {
            if (it.id == taskId) it.copy(
                status = TaskStatus.FAILED,
                errorMessage = error,
                completedAtMillis = System.currentTimeMillis()
            ) else it
        }
    }

    fun cancelTask(taskId: String) {
        _tasksFlow.value = _tasksFlow.value.map {
            if (it.id == taskId) it.copy(
                status = TaskStatus.CANCELLED,
                completedAtMillis = System.currentTimeMillis()
            ) else it
        }
    }

    companion object {
        val instance: TaskManager by lazy { TaskManager() }
    }
}
