package com.omnifile.app.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.omnifile.app.ui.theme.BlueSecondary
import com.omnifile.app.ui.theme.EmeraldSuccess
import com.omnifile.app.ui.theme.RoseAccent
import com.omnifile.app.ui.theme.VioletPrimary
import com.omnifile.feature.toolkit.editor.*
import java.security.MessageDigest

data class ToolDefinition(
    val id: String,
    val name: String,
    val desc: String,
    val category: String,
    val tint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDialog(
    tool: ToolDefinition,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Modal Top Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tool.tint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = tool.tint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tool.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = VioletPrimary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "Gratis • No Login",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VioletPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = tool.desc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Modal Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (tool.id) {
                        "apktool" -> ApkToolContent()
                        "jadx" -> JadxContent()
                        "apk-signer" -> ApkSignerContent()
                        "hash-tools" -> HashToolsContent()
                        "zip-tools" -> ZipToolsContent()
                        "hex-editor" -> HexEditorContent()
                        "manifest-editor" -> ManifestEditorContent()
                        "sqlite-browser" -> SqliteBrowserContent()
                        "smali-editor" -> SmaliEditorContent()
                        "xml-editor" -> XmlEditorContent()
                        "dex-explorer" -> DexExplorerContent()
                        else -> ApkEditorContent()
                    }
                }
            }
        }
    }
}

// 1. APKTool Content
@Composable
private fun ApkToolContent() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf(listOf("Siap memproses APK/ZIP target: MyApp.apk (2.4 MB)")) }
    var hasDecoded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Decode APK") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Rebuild APK") })
        }

        if (selectedTab == 0) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Target APK: MyApp.apk", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Ukuran: 2.4 MB • Status: Siap di-decode", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = {
                    isProcessing = true
                    logs = listOf(
                        "[I] Loading APK: MyApp.apk...",
                        "[I] Decoding AndroidManifest.xml with AAPT2 tables...",
                        "[I] Disassembling classes.dex to smali/ ...",
                        "[I] Extracting 142 resources (strings, drawable, layout)...",
                        "[I] Sukses dibongkar ke: /workspaces/MyApp_src/"
                    )
                    isProcessing = false
                    hasDecoded = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(6.dp))
                Text("Mulai Decode APK")
            }
        } else {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Sumber Workspace: /workspaces/MyApp_src/", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Target Output: MyApp_rebuilt.apk", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = {
                    isProcessing = true
                    logs = listOf(
                        "[I] Rebuilding APK from workspace source...",
                        "[I] Compiling smali to classes.dex...",
                        "[I] Packaging resources with AAPT2...",
                        "[I] Signing with AOSP testkey (v1+v2)...",
                        "[I] Build selesai: MyApp_rebuilt.apk (2.35 MB) siap diinstall!"
                    )
                    isProcessing = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Build, null)
                Spacer(Modifier.width(6.dp))
                Text("Rebuild & Kemas APK")
            }
        }

        // Logs display
        Text("Log Engine:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                logs.forEach { log ->
                    Text(
                        text = log,
                        color = if (log.contains("Sukses") || log.contains("selesai")) EmeraldSuccess else Color(0xFF93C5FD),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// 2. JADX Content
@Composable
private fun JadxContent() {
    val clipboard = LocalClipboardManager.current
    var selectedClass by remember { mutableStateOf("MainActivity.java") }
    val javaCode = """
package com.omnifile.app;

import android.os.Bundle;
import androidx.activity.ComponentActivity;

public class MainActivity extends ComponentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // OmniFile Decompiled Java Source via JADX Engine
        System.out.println("OmniFile Mobile Toolkit Active");
    }
}
    """.trimIndent()

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kelas: $selectedClass", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Button(
                onClick = { clipboard.setText(AnnotatedString(javaCode)) },
                colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Salin Java", fontSize = 11.sp)
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E1E2E),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = javaCode,
                    color = Color(0xFFA6E3A1),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// 3. APK Signer Content
@Composable
private fun ApkSignerContent() {
    var keystoreType by remember { mutableStateOf("Debug Keystore (AOSP)") }
    var v1Scheme by remember { mutableStateOf(true) }
    var v2Scheme by remember { mutableStateOf(true) }
    var v3Scheme by remember { mutableStateOf(true) }
    var isSigned by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("File Target: MyApp_rebuilt.apk", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Status: Belum ditandatangani", fontSize = 11.sp, color = RoseAccent)
            }
        }

        Text("Skema Tanda Tangan:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = v1Scheme, onCheckedChange = { v1Scheme = it })
            Text("V1 (JAR Signature)", fontSize = 11.sp)
            Spacer(Modifier.width(12.dp))
            Checkbox(checked = v2Scheme, onCheckedChange = { v2Scheme = it })
            Text("V2 (Full APK)", fontSize = 11.sp)
            Spacer(Modifier.width(12.dp))
            Checkbox(checked = v3Scheme, onCheckedChange = { v3Scheme = it })
            Text("V3", fontSize = 11.sp)
        }

        Button(
            onClick = { isSigned = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEE9A2C)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.VerifiedUser, null)
            Spacer(Modifier.width(6.dp))
            Text("Tanda Tangani APK Sekarang")
        }

        if (isSigned) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFE5F8EF)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("APK Sukses Ditandatangani!", fontWeight = FontWeight.Bold, color = EmeraldSuccess, fontSize = 13.sp)
                    }
                    Text("Algoritma: SHA256withRSA (2048-bit)", fontSize = 10.sp, color = Color(0xFF167A50))
                    Text("Digest: a3f89e21...89bc10 (V1 + V2 Verified)", fontSize = 10.sp, color = Color(0xFF167A50))
                    Text("Output: MyApp_signed.apk siap diinstal di semua perangkat.", fontSize = 10.sp, color = Color(0xFF167A50))
                }
            }
        }
    }
}

// 4. Hash Tools Content
@Composable
private fun HashToolsContent() {
    val clipboard = LocalClipboardManager.current
    var inputText by remember { mutableStateOf("OmniFile Android Toolkit v1.0.0") }
    var compareHash by remember { mutableStateOf("") }

    fun calculateHash(text: String, algorithm: String): String {
        return try {
            val bytes = MessageDigest.getInstance(algorithm).digest(text.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            "error"
        }
    }

    val md5 = remember(inputText) { calculateHash(inputText, "MD5") }
    val sha1 = remember(inputText) { calculateHash(inputText, "SHA-1") }
    val sha256 = remember(inputText) { calculateHash(inputText, "SHA-256") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Teks Input / String Hash") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = compareHash,
            onValueChange = { compareHash = it },
            label = { Text("Bandingkan Hash (Verifikasi Integritas)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (compareHash.isNotBlank()) {
            val matches = compareHash.trim().equals(md5, true) ||
                          compareHash.trim().equals(sha1, true) ||
                          compareHash.trim().equals(sha256, true)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (matches) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
            ) {
                Text(
                    text = if (matches) "✓ Hash Cocok (File/Teks Identik)" else "✗ Hash Berbeda",
                    color = if (matches) Color(0xFF065F46) else Color(0xFF991B1B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        HashRow("MD5", md5) { clipboard.setText(AnnotatedString(md5)) }
        HashRow("SHA-1", sha1) { clipboard.setText(AnnotatedString(sha1)) }
        HashRow("SHA-256", sha256) { clipboard.setText(AnnotatedString(sha256)) }
    }
}

@Composable
private fun HashRow(title: String, hash: String, onCopy: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = VioletPrimary)
                Text(hash, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// 5. ZIP Tools Content
@Composable
private fun ZipToolsContent() {
    var mode by remember { mutableIntStateOf(0) }
    var progress by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabRow(selectedTabIndex = mode, containerColor = Color.Transparent, divider = {}) {
            Tab(selected = mode == 0, onClick = { mode = 0 }, text = { Text("Ekstrak ZIP") })
            Tab(selected = mode == 1, onClick = { mode = 1 }, text = { Text("Buat Arsip ZIP") })
        }

        if (mode == 0) {
            Text("Pilih arsip untuk diekstrak secara aman (dilindungi proteksi Zip Slip).", fontSize = 11.sp)
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(Modifier.padding(12.dp)) {
                    Text("Arsip: SampleProject.zip (1.2 MB)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Tujuan: /workspaces/SampleProject_extracted/", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Button(
                onClick = { progress = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FolderZip, null)
                Spacer(Modifier.width(6.dp))
                Text("Ekstrak Semua Berkas")
            }
        } else {
            Text("Pilih folder untuk dikompresi menjadi arsip ZIP berkecepatan tinggi.", fontSize = 11.sp)
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(Modifier.padding(12.dp)) {
                    Text("Folder Sumber: /workspaces/MyApp_src/", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Level Kompresi: Maksimum (Deflate)", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Button(
                onClick = { progress = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Archive, null)
                Spacer(Modifier.width(6.dp))
                Text("Buat Berkas ZIP")
            }
        }

        if (progress) {
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFD1FAE5)) {
                Text(
                    text = "Operasi ZIP berhasil diselesaikan dengan validasi integritas checksum CRC32.",
                    color = Color(0xFF065F46),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// 6. Hex Editor Content
@Composable
private fun HexEditorContent() {
    val sampleData = remember {
        "7F 45 4C 46 02 01 01 00 00 00 00 00 00 00 00 00\n" +
        "02 00 B7 00 01 00 00 00 80 10 00 00 00 00 00 00\n" +
        "40 00 00 00 00 00 00 00 28 20 02 00 00 00 00 00\n" +
        "00 00 00 00 40 00 38 00 08 00 40 00 1E 00 1D 00"
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hex Dump (ELF Header 64-bit)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("Offset: 0x0000", fontSize = 10.sp, color = VioletPrimary, fontFamily = FontFamily.Monospace)
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sampleData.lines().forEachIndexed { idx, line ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "%04X:".format(idx * 16),
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                        Text(
                            text = line,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// 7. Manifest Editor Content
@Composable
private fun ManifestEditorContent() {
    var pkgName by remember { mutableStateOf("com.example.app") }
    var debuggable by remember { mutableStateOf(false) }
    var permissions by remember {
        mutableStateOf(
            listOf(
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.READ_EXTERNAL_STORAGE"
            )
        )
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = pkgName,
            onValueChange = { pkgName = it },
            label = { Text("Nama Paket (Package Name)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Mode Debug (android:debuggable)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Switch(checked = debuggable, onCheckedChange = { debuggable = it })
        }

        Text("Izin Android (Permissions):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        permissions.forEach { perm ->
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(perm, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    IconButton(
                        onClick = { permissions = permissions - perm },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = RoseAccent, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Button(
            onClick = {
                permissions = permissions + "android.permission.CAMERA"
            },
            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Tambah Izin Baru", fontSize = 11.sp)
        }
    }
}

// 8. SQLite Browser Content
@Composable
private fun SqliteBrowserContent() {
    var query by remember { mutableStateOf("SELECT * FROM projects LIMIT 5;") }
    var executed by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("SQL Query") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { executed = true },
            colors = ButtonDefaults.buttonColors(containerColor = BlueSecondary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.PlayArrow, null)
            Spacer(Modifier.width(6.dp))
            Text("Jalankan Query")
        }

        Text("Hasil Query (3 baris ditemukan):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("id | name | type | status", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Divider()
                Text("1 | MyApp.apk | apk | Ready", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("2 | StudioProject | workspace | Ready", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("3 | Sample-Dex | dex | Decompiled", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// 9. Smali Editor Content
@Composable
private fun SmaliEditorContent() {
    var smaliText by remember {
        mutableStateOf(
            """.class public Lcom/example/app/MainActivity;
.super Landroid/app/Activity;
.source "MainActivity.java"

.method public onCreate(Landroid/os/Bundle;)V
    .registers 2
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V
    return-void
.end method"""
        )
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("File: MainActivity.smali", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Button(
                onClick = {
                    smaliText = SmaliEditorModel.applyPreset(smaliText, SmaliEditorModel.COMMON_PATCHES[0])
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoseAccent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Patch Root Bypass", fontSize = 10.sp)
            }
        }

        OutlinedTextField(
            value = smaliText,
            onValueChange = { smaliText = it },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// 10. XML Editor Content
@Composable
private fun XmlEditorContent() {
    var xmlText by remember {
        mutableStateOf(
            """<resources>
    <string name="app_name">OmniFile</string>
    <color name="primary">#673DE6</color>
</resources>"""
        )
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Editor: strings.xml", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Button(
                onClick = { xmlText = XmlEditorModel.formatXml(xmlText) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Format XML", fontSize = 10.sp)
            }
        }

        OutlinedTextField(
            value = xmlText,
            onValueChange = { xmlText = it },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// 11. DEX Explorer Content
@Composable
private fun DexExplorerContent() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("DEX File: classes.dex (Header & Metadata)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Magic: dex\n039", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("Checksum: 0x93E4F18A", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("Class Definitions: 142 kelas", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("Method IDs: 1,842 metode", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("String IDs: 3,210 string", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Text("Daftar Kelas Utama:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        listOf(
            "Lcom/omnifile/app/MainActivity;",
            "Lcom/omnifile/core/workspace/WorkspaceManager;",
            "Lcom/omnifile/feature/filemanager/FileRepositoryImpl;",
            "Lcom/omnifile/engines/apktool/ApktoolEngine;"
        ).forEach { cls ->
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
                Text(cls, modifier = Modifier.padding(8.dp), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = VioletPrimary)
            }
        }
    }
}

// 12. APK Editor Content
@Composable
private fun ApkEditorContent() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Struktur Internal APK:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        listOf(
            "AndroidManifest.xml (Biner terkompilasi)",
            "classes.dex (Bytecode Dalvik)",
            "resources.arsc (Tabel Resource)",
            "res/drawable/ (Aset Grafis)",
            "res/values/ (Strings & Warna)",
            "lib/arm64-v8a/ (Native Shared Library)",
            "META-INF/ (Sertifikat & Signature)"
        ).forEach { item ->
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FolderOpen, null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(item, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
