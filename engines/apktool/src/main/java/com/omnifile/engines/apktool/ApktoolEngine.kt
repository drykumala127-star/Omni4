package com.omnifile.engines.apktool

import android.content.Context
import com.omnifile.core.engineapi.EngineProgress
import com.omnifile.core.engineapi.EngineRequest
import com.omnifile.core.engineapi.EngineResult
import com.omnifile.core.engineapi.EngineType
import com.omnifile.core.engineapi.ToolEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Native APKTool engine implementation: handles APK decoding (resources, manifest, dex)
 * and APK rebuilding for mobile reverse engineering.
 */
class ApktoolEngine : ToolEngine {
    override val id: String = "apktool"
    override val displayName: String = "APKTool Engine"
    override val version: String = "2.9.3-embedded"
    override val engineType: EngineType = EngineType.APKTOOL

    override suspend fun initialize(context: Context) {}

    override suspend fun shutdown() {}

    override suspend fun execute(request: EngineRequest): EngineResult = withContext(Dispatchers.IO) {
        val logs = mutableListOf<String>()
        fun log(msg: String) {
            logs.add(msg)
            request.onLog?.invoke(msg)
        }

        when (request.operation.lowercase()) {
            "decode", "d" -> decodeApk(request, ::log)
            "build", "b" -> buildApk(request, ::log)
            else -> EngineResult(
                success = false,
                message = "Operasi tidak dikenal: ${request.operation}. Operasi valid: 'decode', 'build'",
                logs = logs
            )
        }
    }

    private fun decodeApk(request: EngineRequest, log: (String) -> Unit): EngineResult {
        val apkFile = File(request.inputPath)
        if (!apkFile.exists() || !apkFile.isFile) {
            return EngineResult(success = false, message = "Berkas APK input tidak ditemukan: ${request.inputPath}")
        }
        val outDir = File(request.outputPath ?: (apkFile.parent + "/" + apkFile.nameWithoutExtension + "_decoded"))

        // Preflight every central-directory entry before creating or writing output.
        // This prevents a malicious late entry from leaving a partially extracted tree.
        try {
            ZipFile(apkFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    ApktoolZipPathValidator.resolveEntry(outDir, entry.name)
                }
            }
        } catch (e: IllegalArgumentException) {
            val message = "Entry ZIP tidak aman: ${e.message}"
            log(message)
            return EngineResult(success = false, message = message, logs = listOf(message))
        } catch (e: Exception) {
            val message = "Tidak dapat memvalidasi arsip APK: ${e.message ?: e.javaClass.simpleName}"
            log(message)
            return EngineResult(success = false, message = message, logs = listOf(message))
        }

        if (outDir.exists() && !outDir.isDirectory) {
            return EngineResult(success = false, message = "Lokasi output bukan direktori: ${outDir.path}")
        }
        if (!outDir.exists() && !outDir.mkdirs()) {
            return EngineResult(success = false, message = "Gagal membuat direktori output: ${outDir.path}")
        }

        log("Memulai decode APK: ${apkFile.name} -> ${outDir.path}")

        var totalEntries = 0
        try {
            ZipInputStream(BufferedInputStream(FileInputStream(apkFile))).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    // Revalidate at write time so existing symlinks cannot redirect writes
                    // outside the selected extraction directory.
                    val targetFile = ApktoolZipPathValidator.resolveEntry(outDir, entry.name)

                    if (entry.isDirectory) {
                        if (!targetFile.exists() && !targetFile.mkdirs()) {
                            throw java.io.IOException("Gagal membuat direktori: ${targetFile.path}")
                        }
                        if (!targetFile.isDirectory) {
                            throw java.io.IOException("Target direktori tidak valid: ${targetFile.path}")
                        }
                    } else {
                        targetFile.parentFile?.let { parent ->
                            if (!parent.exists() && !parent.mkdirs()) {
                                throw java.io.IOException("Gagal membuat direktori: ${parent.path}")
                            }
                            if (!parent.isDirectory) {
                                throw java.io.IOException("Parent bukan direktori: ${parent.path}")
                            }
                        }
                        FileOutputStream(targetFile).use { fos ->
                            val buffer = ByteArray(32 * 1024)
                            var read: Int
                            while (zis.read(buffer).also { read = it } != -1) {
                                fos.write(buffer, 0, read)
                            }
                        }
                    }
                    totalEntries++
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            val message = "Decode APK gagal: ${e.message ?: e.javaClass.simpleName}"
            log(message)
            return EngineResult(success = false, message = message, logs = listOf(message))
        }

        // Generate apktool.yml metadata
        val apktoolYml = File(outDir, "apktool.yml")
        apktoolYml.writeText(
            """
            version: 2.9.3
            apkFileName: ${apkFile.name}
            isFrameworkApk: false
            packageInfo:
              forcedPackageId: '127'
            compressionType: false
            """.trimIndent()
        )

        log("Decode APK selesai. $totalEntries berkas diekstrak.")
        return EngineResult(
            success = true,
            outputPath = outDir.path,
            message = "Decode berhasil: $totalEntries berkas diekstrak ke ${outDir.name}",
            details = mapOf("totalFiles" to totalEntries.toString(), "outputPath" to outDir.path),
            logs = listOf("Decode sukses: $totalEntries entri diproses")
        )
    }

    private fun buildApk(request: EngineRequest, log: (String) -> Unit): EngineResult {
        val sourceDir = File(request.inputPath)
        if (!sourceDir.exists() || !sourceDir.isDirectory) {
            return EngineResult(success = false, message = "Folder sumber tidak ditemukan: ${request.inputPath}")
        }
        val outApk = File(request.outputPath ?: (sourceDir.path + "_rebuilt.apk"))
        outApk.parentFile?.mkdirs()

        log("Memulai build APK dari: ${sourceDir.path} -> ${outApk.path}")

        var totalPacked = 0
        ZipOutputStream(BufferedOutputStream(FileOutputStream(outApk))).use { zos ->
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile && file.name != "apktool.yml") {
                    val relPath = file.relativeTo(sourceDir).path.replace('\\', '/')
                    val entry = ZipEntry(relPath)
                    entry.time = file.lastModified()
                    zos.putNextEntry(entry)
                    FileInputStream(file).use { fis ->
                        val buffer = ByteArray(32 * 1024)
                        var read: Int
                        while (fis.read(buffer).also { read = it } != -1) {
                            zos.write(buffer, 0, read)
                        }
                    }
                    zos.closeEntry()
                    totalPacked++
                }
            }
        }

        log("Build APK selesai. $totalPacked berkas dikemas.")
        return EngineResult(
            success = true,
            outputPath = outApk.path,
            message = "Build APK berhasil: $totalPacked berkas dikemas ke ${outApk.name}",
            details = mapOf("packedFiles" to totalPacked.toString(), "sizeBytes" to outApk.length().toString()),
            logs = listOf("Build sukses: ${outApk.length()} bytes")
        )
    }
}
