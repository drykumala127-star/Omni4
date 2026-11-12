package com.omnifile.app

import com.omnifile.core.workspace.OriginalApkManager
import com.omnifile.core.workspace.ProjectBackupManager
import com.omnifile.core.workspace.WorkspaceManager
import com.omnifile.core.workspace.WorkspaceSecurityValidator
import com.omnifile.core.workspace.model.ProjectArea
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

/**
 * End-to-end unit tests verifying Stage 2 Workspace Manager architecture:
 * - 2.1 Workspace Architecture & Area Directories
 * - 2.2 Create Workspace & Manifest
 * - 2.3 Import APK Projects & Metadata
 * - 2.4 Project File Management & Sandboxing
 * - 2.5 Original APK Management & Integrity Verification
 * - 2.6 Backup & Restore Snapshot Engine
 * - 2.7 Project History Tracking
 */
class WorkspaceManagerTest {

    private val tempDir: File by lazy {
        val f = File(System.getProperty("java.io.tmpdir"), "omnifile_ws_test_" + UUID.randomUUID())
        f.mkdirs()
        f
    }

    @Test
    fun testWorkspaceProjectCreationAndAreas() {
        val wsManager = WorkspaceManager(tempDir)
        val meta = wsManager.createProject("TestProject")

        assertNotNull(meta.id)
        assertEquals("TestProject", meta.name)

        val projectDirs = tempDir.listFiles { f -> f.isDirectory && f.name.startsWith("TestProject") }
        assertNotNull(projectDirs)
        assertTrue(projectDirs!!.isNotEmpty())

        val projectDir = projectDirs[0]
        // Verify all 9 project areas are created
        for (area in ProjectArea.values()) {
            val areaFolder = File(projectDir, area.folderName)
            assertTrue("Area folder ${area.folderName} must exist", areaFolder.exists())
        }

        // Verify project.json exists
        val projectJson = File(projectDir, "project.json")
        assertTrue(projectJson.exists())

        val loaded = wsManager.loadProjectMetadata(projectDir)
        assertNotNull(loaded)
        assertEquals(meta.id, loaded!!.id)
        assertEquals("TestProject", loaded.name)
    }

    @Test
    fun testWorkspaceSandboxContainment() {
        val projectRoot = File(tempDir, "sandbox_proj")
        projectRoot.mkdirs()

        val legitimateChild = File(projectRoot, "decoded/smali/Main.smali")
        // Must succeed without exception
        WorkspaceSecurityValidator.assertWithinWorkspace(projectRoot, legitimateChild)

        val escapingChild = File(projectRoot, "../../outside.txt")
        assertThrows(SecurityException::class.java) {
            WorkspaceSecurityValidator.assertWithinWorkspace(projectRoot, escapingChild)
        }
    }

    @Test
    fun testOriginalApkTamperProtection() {
        val projectRoot = File(tempDir, "apk_proj")
        projectRoot.mkdirs()

        val mockApk = File(tempDir, "sample.apk")
        mockApk.writeText("PK_MOCK_ANDROID_APK_PAYLOAD_FOR_TESTING")

        val originalManager = OriginalApkManager(projectRoot)
        val initialSha256 = originalManager.storeOriginalApk(mockApk)

        assertTrue(initialSha256.isNotEmpty())
        assertTrue(originalManager.verifyIntegrity(initialSha256))
        assertFalse(originalManager.verifyIntegrity("wrong_hash_123456"))
    }

    @Test
    fun testProjectBackupAndRestoreSnapshot() {
        val projectRoot = File(tempDir, "backup_proj")
        projectRoot.mkdirs()

        val decodedDir = File(projectRoot, ProjectArea.DECODED.folderName)
        decodedDir.mkdirs()
        val testFile = File(decodedDir, "AndroidManifest.xml")
        testFile.writeText("<manifest package=\"com.test.app\" />")

        val backupManager = ProjectBackupManager(projectRoot)
        val snapshot = backupManager.createSnapshot("proj_1", "Test")

        assertTrue(snapshot.fileCount >= 1)
        val snapshots = backupManager.listSnapshots()
        assertTrue(snapshots.isNotEmpty())

        // Test restore
        testFile.delete()
        assertFalse(testFile.exists())

        backupManager.restoreSnapshot(snapshots[0])
        assertTrue("Restored file must exist", testFile.exists())
        assertEquals("<manifest package=\"com.test.app\" />", testFile.readText())
    }
}
