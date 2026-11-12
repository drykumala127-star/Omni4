package com.omnifile.app

import com.omnifile.feature.toolkit.system.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

/**
 * End-to-end unit tests verifying Stage 6 Additional System:
 * - Task Manager
 * - File Comparison Engine
 * - Batch Processing
 * - Crash Recovery Manager
 */
class AdditionalSystemsTest {

    private val tempDir: File by lazy {
        val f = File(System.getProperty("java.io.tmpdir"), "add_sys_test_" + UUID.randomUUID())
        f.mkdirs()
        f
    }

    @Test
    fun testTaskManagerLifecycle() {
        val tm = TaskManager.instance
        val task = tm.addTask("APK Decompile Job")
        assertEquals(TaskStatus.RUNNING, task.status)

        tm.updateProgress(task.id, 50)
        val inProgress = tm.tasksFlow.value.first { it.id == task.id }
        assertEquals(50, inProgress.progressPercent)

        tm.markComplete(task.id)
        val finished = tm.tasksFlow.value.first { it.id == task.id }
        assertEquals(TaskStatus.COMPLETED, finished.status)
        assertEquals(100, finished.progressPercent)
        assertNotNull(finished.completedAtMillis)
    }

    @Test
    fun testFileComparisonEngineDiff() {
        val original = "line1\nline2\nline3"
        val modified = "line1\nline2_modified\nline3\nline4_added"

        val result = FileComparisonEngine.compareTexts(original, modified)
        assertFalse(result.areIdentical)
        assertTrue(result.additionsCount >= 1)
        assertTrue(result.diffLines.isNotEmpty())

        val identicalResult = FileComparisonEngine.compareTexts("same\ntext", "same\ntext")
        assertTrue(identicalResult.areIdentical)
        assertEquals(0, identicalResult.additionsCount)
        assertEquals(0, identicalResult.deletionsCount)
    }

    @Test
    fun testBatchProcessorRenameRules() {
        val files = listOf("icon.png", "logo.png", "bg.png")
        val rule = BatchRenameRule(prefix = "app_", suffix = "_v1", startNumberIndex = 1)
        val renamed = BatchProcessor.generateNewNames(files, rule)

        assertEquals(3, renamed.size)
        assertEquals("app_icon_v1_1.png", renamed[0].second)
        assertEquals("app_logo_v1_2.png", renamed[1].second)
        assertEquals("app_bg_v1_3.png", renamed[2].second)
    }

    @Test
    fun testCrashRecoveryCheckpoint() {
        val manager = CrashRecoveryManager(tempDir)
        val checkpoint = CrashRecoveryCheckpoint(
            projectId = "proj_test_1",
            activeFilePath = "decoded/res/values/strings.xml",
            unsavedContent = "<resources><string name=\"title\">OmniFile</string></resources>"
        )

        manager.saveCheckpoint(checkpoint)
        val loaded = manager.loadCheckpoint("proj_test_1")
        assertNotNull(loaded)
        assertEquals(checkpoint.activeFilePath, loaded!!.activeFilePath)
        assertEquals(checkpoint.unsavedContent, loaded.unsavedContent)

        manager.clearCheckpoint("proj_test_1")
        val cleared = manager.loadCheckpoint("proj_test_1")
        assertNull(cleared)
    }
}
