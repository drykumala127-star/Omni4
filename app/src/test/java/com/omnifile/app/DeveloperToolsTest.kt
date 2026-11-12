package com.omnifile.app

import com.omnifile.feature.toolkit.editor.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

/**
 * End-to-end unit tests verifying Stage 4 Editor and Developer Tools:
 * - 4.1 XML Editor (Formatting & Validation)
 * - 4.2 Smali Editor (Patch Presets)
 * - 4.4 Manifest Editor (Permission Manager & Debuggable Toggle)
 * - 4.6 HEX Viewer & Editor (16-byte Dump & Byte Modify)
 * - 4.7 SQLite Browser (Header Magic & Schema Parsing)
 * - 4.8 Code Search Engine (Multi-file regex search)
 */
class DeveloperToolsTest {

    @Test
    fun testXmlFormattingAndValidation() {
        val rawXml = "<manifest><application android:label=\"OmniFile\"><activity/></application></manifest>"
        val formatted = XmlEditorModel.formatXml(rawXml)
        assertTrue(formatted.contains("\n"))

        val (isValid, err) = XmlEditorModel.validateXmlSyntax(formatted)
        assertTrue(isValid)
        assertNull(err)

        val invalidXml = "<manifest><unclosed></manifest>"
        val (isBadValid, badErr) = XmlEditorModel.validateXmlSyntax(invalidXml)
        assertFalse(isBadValid)
        assertNotNull(badErr)
    }

    @Test
    fun testSmaliPatchPresets() {
        val originalSmali = """
            .method public isDeviceRooted()Z
                .registers 2
                invoke-static {}, Ltest/RootUtil;->checkSu()Z
                move-result v0
                return v0
            .end method
        """.trimIndent()

        val patch = SmaliEditorModel.COMMON_PATCHES.first { it.id == "root_bypass" }
        val patched = SmaliEditorModel.applyPreset(originalSmali, patch)

        assertTrue(patched.contains("const/4 v0, 0x0"))
        assertTrue(patched.contains("Applied OmniFile Patch: Bypass Deteksi Root"))
    }

    @Test
    fun testManifestEditorPermissions() {
        val manifest = """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.test.app">
                <uses-permission android:name="android.permission.INTERNET" />
                <application android:label="App">
                </application>
            </manifest>
        """.trimIndent()

        val perms = ManifestEditorModel.extractPermissions(manifest)
        assertEquals(listOf("android.permission.INTERNET"), perms)

        val withStorage = ManifestEditorModel.addPermission(manifest, "android.permission.READ_EXTERNAL_STORAGE")
        val updatedPerms = ManifestEditorModel.extractPermissions(withStorage)
        assertTrue(updatedPerms.contains("android.permission.READ_EXTERNAL_STORAGE"))

        val removed = ManifestEditorModel.removePermission(withStorage, "android.permission.INTERNET")
        val afterRemovePerms = ManifestEditorModel.extractPermissions(removed)
        assertFalse(afterRemovePerms.contains("android.permission.INTERNET"))
        assertTrue(afterRemovePerms.contains("android.permission.READ_EXTERNAL_STORAGE"))
    }

    @Test
    fun testHexDumpGeneratorAndEditing() {
        val sample = "Hello OmniFile!".toByteArray(Charsets.US_ASCII)
        val dump = HexEditorModel.generateHexDump(sample)
        assertEquals(1, dump.size)
        assertTrue(dump[0].asciiString.contains("Hello OmniFile!"))
        assertTrue(dump[0].hexString.contains("48 65 6C 6C 6F"))

        val modified = HexEditorModel.modifyByte(sample, 0, 'J'.code.toByte())
        assertEquals("Jello OmniFile!", String(modified, Charsets.US_ASCII))
    }

    @Test
    fun testSqliteMagicHeaderValidation() {
        val tempFile = File.createTempFile("test_sqlite", ".db")
        tempFile.writeBytes(SqliteBrowserModel.SQLITE_HEADER_MAGIC + ByteArray(100))
        assertTrue(SqliteBrowserModel.isSqliteDatabase(tempFile))

        val nonSqlite = File.createTempFile("not_sqlite", ".txt")
        nonSqlite.writeText("PLAIN_TEXT_FILE")
        assertFalse(SqliteBrowserModel.isSqliteDatabase(nonSqlite))

        tempFile.delete()
        nonSqlite.delete()
    }

    @Test
    fun testCodeSearchEngine() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "search_test_" + UUID.randomUUID())
        tempDir.mkdirs()
        val f1 = File(tempDir, "A.kt")
        f1.writeText("val secretApiKey = \"12345\"\nprintln(\"Hello\")")
        val f2 = File(tempDir, "B.smali")
        f2.writeText("const-string v0, \"secretApiKey\"\nreturn-void")

        val results = CodeSearchEngine.searchDirectory(tempDir, "secretApiKey")
        assertEquals(2, results.size)
        assertEquals(1, results[0].lineNumber)
        assertEquals(1, results[1].lineNumber)

        tempDir.deleteRecursively()
    }
}
