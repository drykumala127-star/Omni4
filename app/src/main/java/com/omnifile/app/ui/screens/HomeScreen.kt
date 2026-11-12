package com.omnifile.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.omnifile.app.ui.theme.BlueSecondary
import com.omnifile.app.ui.theme.EmeraldSuccess
import com.omnifile.app.ui.theme.VioletPrimary
import com.omnifile.app.ui.tools.ToolDefinition

private data class QuickActionItem(
    val id: String,
    val name: String,
    val desc: String,
    val icon: ImageVector,
    val tint: Color
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToTab: (Int) -> Unit,
    onOpenTool: (ToolDefinition) -> Unit
) {
    val quickActions = listOf(
        QuickActionItem("files", "File Manager", "Kelola berkas", Icons.Default.Folder, Color(0xFFFFA126)),
        QuickActionItem("apktool", "APKTool", "Bongkar & rakit", Icons.Default.Android, EmeraldSuccess),
        QuickActionItem("dex-explorer", "DEX Explorer", "Bytecode Dalvik", Icons.Default.Code, BlueSecondary),
        QuickActionItem("smali-editor", "Smali Editor", "Edit instruksi", Icons.Default.Terminal, Color(0xFFFF5D67))
    )

    ScreenColumn(modifier = modifier) {
        GradientBanner(
            title = "Jelajahi Potensi Tanpa Batas",
            subtitle = "Toolkit profesional dan File Manager untuk modifikasi, reverse engineering, dan pengembangan Android dalam satu workspace.",
            actionText = "Mulai Jelajahi Tools →",
            onActionClick = { onNavigateToTab(1) }
        )

        SectionTitle(title = "Aksi Cepat", action = "Semua Tools", onActionClick = { onNavigateToTab(1) })

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(105.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(quickActions) { action ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("quick_action_${action.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (action.id == "files") {
                                    onNavigateToTab(2)
                                } else {
                                    onOpenTool(
                                        ToolDefinition(
                                            id = action.id,
                                            name = action.name,
                                            desc = action.desc,
                                            category = "reversing",
                                            tint = action.tint
                                        )
                                    )
                                }
                            }
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(action.tint.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(action.icon, null, tint = action.tint, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = action.name,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        SectionTitle(title = "Proyek Terakhir", action = "Lihat Semua", onActionClick = { onNavigateToTab(3) })

        HomeProjectItem(
            name = "MyApp.apk",
            detail = "2.4 MB • APK Project • Terakhir dibuka hari ini",
            icon = Icons.Default.Android,
            tint = EmeraldSuccess,
            onClick = { onNavigateToTab(3) }
        )

        HomeProjectItem(
            name = "StudioProject",
            detail = "8.7 MB • Development Workspace • Siap kompilasi",
            icon = Icons.Default.FolderSpecial,
            tint = VioletPrimary,
            onClick = { onNavigateToTab(3) }
        )

        SectionTitle(title = "Status Sistem")

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE5F8EF),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = EmeraldSuccess, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Semua sistem berjalan normal",
                        color = Color(0xFF167A50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Engine lokal siap, SAF storage aktif, dan 0 dependensi eksternal.",
                        color = Color(0xFF167A50).copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(60.dp))
    }
}

@Composable
private fun HomeProjectItem(
    name: String,
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
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}
