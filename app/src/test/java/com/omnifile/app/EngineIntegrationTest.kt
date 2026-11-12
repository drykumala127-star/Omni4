package com.omnifile.app

import com.omnifile.core.engineapi.ApkAnalyzer
import com.omnifile.core.engineapi.EngineManager
import com.omnifile.core.engineapi.EngineRequest
import com.omnifile.engines.apktool.ApktoolEngine
import com.omnifile.engines.jadx.JadxEngine
import com.omnifile.engines.smali.DexToolkit
import com.omnifile.engines.smali.SmaliEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * End-to-end integration tests for Stage 3 APK Engine Integration:
 * - 3.1 Engine API & Registry
 * - 3.2 Engine Manager Execution
 * - 3.3 Apktool Engine (Decode & Build)
 * - 3.4 Jadx Engine (Decompilation)
 * - 3.5 Dex Toolkit (Header & Signature Verification)
 * - 3.6 Smali Engine (Disassembly & Assembly)
 * - 3.7 APK Analyzer (Structure & Security Inspection)
 */
class EngineIntegrationTest {

    private val tempDir: File by lazy {
        val f = File(System.getProperty("java.io.tmpdir"), "engine_test_" + UUID.randomUUID())
        f.mkdirs()
        f
    }

    @Test
    fun testEngineRegistryAndManager() = runBlocking {
        val manager = EngineManager.instance
        val apktool = ApktoolEngine()
        val jadx = JadxEngine()
        val smali = SmaliEngine()

        manager.register(apktool)
        manager.register(jadx)
        manager.register(smali)

        assertNotNull(manager.get("apktool"))
        assertNotNull(manager.get("jadx"))
        assertNotNull(manager.get("smali"))
        assertTrue(manager.all().size >= 3)
    }

    @Test
    fun testApktoolDecodeAndBuildCycle() = runBlocking {
        // Create mock APK
        val mockApk = File(tempDir, "sample_app.apk")
        ZipOutputStream(mockApk.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("AndroidManifest.xml"))
            zos.write("<manifest package=\"com.test.app\"/>".toByteArray())
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("res/values/strings.xml"))
            zos.write("<resources><string name=\"app\">Test</string></resources>".toByteArray())
            zos.closeEntry()
        }

        val apktool = ApktoolEngine()
        val decodeDir = File(tempDir, "sample_decoded")
        val decodeReq = EngineRequest(
            inputPath = mockApk.path,
            outputPath = decodeDir.path,
            operation = "decode"
        )
        val decodeRes = apktool.execute(decodeReq)
        assertTrue(decodeRes.success)
        assertTrue(File(decodeDir, "AndroidManifest.xml").exists())
        assertTrue(File(decodeDir, "apktool.yml").exists())

        // Now test rebuild
        val rebuiltApk = File(tempDir, "sample_rebuilt.apk")
        val buildReq = EngineRequest(
            inputPath = decodeDir.path,
            outputPath = rebuiltApk.path,
            operation = "build"
        )
        val buildRes = apktool.execute(buildReq)
        assertTrue(buildRes.success)
        assertTrue(rebuiltApk.exists())
        assertTrue(rebuiltApk.length() > 0)
    }

    @Test
    fun testApktoolDecodeRejectsZipSlipBeforeWritingAnything() = runBlocking {
        val maliciousApk = File(tempDir, "zip_slip.apk")
        ZipOutputStream(maliciousApk.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("safe/first.txt"))
            zos.write("must-not-be-written".toByteArray())
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("../../${tempDir.name}_escaped.txt"))
            zos.write("outside".toByteArray())
            zos.closeEntry()
        }

        val outputDir = File(tempDir, "zip_slip_output")
        val outsideFile = File(tempDir.parentFile, "${tempDir.name}_escaped.txt")
        outsideFile.delete()
        val result = ApktoolEngine().execute(
            EngineRequest(inputPath = maliciousApk.path, outputPath = outputDir.path, operation = "decode")
        )

        assertFalse("Zip Slip archive must be rejected", result.success)
        assertFalse("Preflight must happen before output directory creation", outputDir.exists())
        assertFalse("No file may be written outside extraction root", outsideFile.exists())
    }

    @Test
    fun testApktoolDecodeRejectsWindowsAndBackslashTraversal() = runBlocking {
        val maliciousApk = File(tempDir, "windows_zip_slip.apk")
        ZipOutputStream(maliciousApk.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("..\\..\\escaped.txt"))
            zos.write("outside".toByteArray())
            zos.closeEntry()
        }
        val outputDir = File(tempDir, "windows_zip_slip_output")
        val result = ApktoolEngine().execute(
            EngineRequest(inputPath = maliciousApk.path, outputPath = outputDir.path, operation = "decode")
        )
        assertFalse(result.success)
        assertFalse(outputDir.exists())
    }

    @Test
    fun testDexToolkitAndSmaliEngine() = runBlocking {
        val smaliDir = File(tempDir, "smali_src")
        smaliDir.mkdirs()
        val smaliFile = File(smaliDir, "Main.smali")
        smaliFile.writeText(".class public Lcom/test/Main;\n.super Ljava/lang/Object;\n")

        val smaliEngine = SmaliEngine()
        val outputDex = File(tempDir, "output.dex")
        val assembleReq = EngineRequest(
            inputPath = smaliDir.path,
            outputPath = outputDex.path,
            operation = "assemble"
        )
        val assembleRes = smaliEngine.execute(assembleReq)
        assertTrue(assembleRes.success)
        assertTrue(outputDex.exists())

        // Parse assembled DEX header
        val header = DexToolkit.parseHeader(outputDex)
        assertTrue("DEX magic must be valid", header.isValidMagic)
        assertTrue("DEX checksum must match", header.isValidChecksum)
        assertTrue("DEX SHA-1 signature must match", header.isValidSignature)
    }

    @Test
    fun testApkAnalyzer() {
        val mockApk = File(tempDir, "analyzer_test.apk")
        ZipOutputStream(mockApk.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("classes.dex"))
            zos.write(ByteArray(112))
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("lib/arm64-v8a/libtest.so"))
            zos.write(ByteArray(256))
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
            zos.write("Manifest-Version: 1.0\n".toByteArray())
            zos.closeEntry()
            zos.putNextEntry(ZipEntry("META-INF/CERT.RSA"))
            zos.write(ByteArray(128))
            zos.closeEntry()
        }

        val report = ApkAnalyzer.analyze(mockApk)
        assertEquals(mockApk.name, report.fileName)
        assertTrue(report.dexFiles.contains("classes.dex"))
        assertTrue(report.nativeAbis.contains("arm64-v8a"))
        assertTrue(report.isSigned)
    }
}
