package com.omnifile.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnifile.app.ui.components.GradientBanner
import com.omnifile.app.ui.components.ScreenColumn
import com.omnifile.app.ui.components.SectionTitle
import com.omnifile.app.ui.theme.AppThemeMode
import com.omnifile.app.ui.theme.BlueSecondary
import com.omnifile.app.ui.theme.EmeraldSuccess
import com.omnifile.app.ui.theme.VioletPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    currentLang: String,
    onToggleLang: () -> Unit,
    onOpenTerminal: () -> Unit,
    onOpenHardening: () -> Unit,
    onOpenRoadmap: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showTaskManagerDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    ScreenColumn(modifier = modifier) {
        GradientBanner(
            title = "OmniFile Developer Suite",
            subtitle = "Aplikasi 100% gratis, lokal, dan privat. Tanpa akun, tanpa analitik eksternal, dan tanpa login.",
            actionText = "Terminal CLI →",
            onActionClick = onOpenTerminal
        )

        SectionTitle(title = "Tema & Tampilan")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(
                AppThemeMode.VIOLET to "Violet (Default)",
                AppThemeMode.DARK to "Dark Night",
                AppThemeMode.INDIGO to "Cyber Indigo"
            ).forEach { (mode, label) ->
                FilterChip(
                    selected = currentTheme == mode,
                    onClick = { onSelectTheme(mode) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (currentTheme == mode) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VioletPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        SectionTitle(title = "Alat Sistem & Preferensi")

        MoreItemRow(
            title = "Engine Terminal CLI",
            detail = "Jalankan perintah apktool, jadx, hash, dan inspeksi",
            icon = Icons.Default.Terminal,
            tint = EmeraldSuccess,
            onClick = onOpenTerminal
        )

        MoreItemRow(
            title = "Verifikasi Hardening & Rilis Final",
            detail = "Status audit keamanan SAF, engine sandbox, dan rilis v1.0.0",
            icon = Icons.Default.VerifiedUser,
            tint = BlueSecondary,
            onClick = onOpenHardening
        )

        MoreItemRow(
            title = "Roadmap Pengembangan (100%)",
            detail = "Status pengerjaan 6 tahapan arsitektur aplikasi",
            icon = Icons.Default.EmojiEvents,
            tint = Color(0xFFD97706),
            onClick = onOpenRoadmap
        )

        MoreItemRow(
            title = "Task Manager",
            detail = "Pantau proses latar belakang dan utilitas engine",
            icon = Icons.Default.Speed,
            tint = VioletPrimary,
            onClick = { showTaskManagerDialog = true }
        )

        MoreItemRow(
            title = "Workspace & Backup",
            detail = "Buat cadangan zip seluruh proyek dan ekspor",
            icon = Icons.Default.Backup,
            tint = BlueSecondary,
            onClick = { showBackupDialog = true }
        )

        MoreItemRow(
            title = "Tentang OmniFile",
            detail = "Versi 1.0.0 Final, lisensi komponen, dan dokumentasi",
            icon = Icons.Default.Info,
            tint = Color.Gray,
            onClick = { showAboutDialog = true }
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "Gratis untuk digunakan • Tidak perlu login • Tidak ada paket berlangganan",
                modifier = Modifier.padding(14.dp),
                color = VioletPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(60.dp))
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Tentang OmniFile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("OmniFile Mobile Toolkit v1.0.0 Final", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Aplikasi Android lengkap untuk manajemen file, analisis APK, reverse engineering, dan pengeditan biner.", fontSize = 11.sp)
                    Divider()
                    Text("Komponen & Lisensi:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("• APKTool (Apache 2.0)", fontSize = 10.sp)
                    Text("• JADX Decompiler (Apache 2.0)", fontSize = 10.sp)
                    Text("• Smali/Baksmali (BSD 3-Clause)", fontSize = 10.sp)
                    Text("• Jetpack Compose & M3 (Apache 2.0)", fontSize = 10.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    if (showTaskManagerDialog) {
        AlertDialog(
            onDismissRequest = { showTaskManagerDialog = false },
            title = { Text("Task Manager", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Proses Latar Belakang Aktif:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Engine Watcher Daemon", fontSize = 11.sp)
                            Text("IDLE", fontSize = 10.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SAF Cache Cleaner", fontSize = 11.sp)
                            Text("OK", fontSize = 10.sp, color = BlueSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showTaskManagerDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Backup Workspace", fontWeight = FontWeight.Bold) },
            text = {
                Text("Semua berkas di folder workspace lokal terlindungi dan dapat dicadangkan sebagai arsip .zip terenkapsulasi secara offline.", fontSize = 12.sp)
            },
            confirmButton = {
                Button(
                    onClick = { showBackupDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text("Cadangkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun MoreItemRow(
    title: String,
    detail: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(detail, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
