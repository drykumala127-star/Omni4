package com.omnifile.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

data class AppProject(
    val id: String,
    val name: String,
    val type: String, // "apk", "workspace", "dex", "resource"
    val detail: String,
    val size: String,
    val lastModified: String,
    val status: String // "Ready", "Compiling", "Signed", "Decompiled"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    onOpenProject: (AppProject) -> Unit
) {
    var projectsList by remember {
        mutableStateOf(
            listOf(
                AppProject("p1", "MyApp.apk", "apk", "2.4 MB • APK Project", "2.4 MB", "Hari ini, 10:45", "Ready"),
                AppProject("p2", "StudioProject", "workspace", "8.7 MB • Development Workspace", "8.7 MB", "Kemarin, 16:20", "Ready"),
                AppProject("p3", "Sample-Dex", "dex", "1.1 MB • Analisis bytecode Dalvik", "1.1 MB", "3 hari yang lalu", "Decompiled"),
                AppProject("p4", "Resource-Edit", "resource", "4.5 MB • XML dan grafis biner", "4.5 MB", "5 hari yang lalu", "Signed")
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("Semua") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filters = listOf("Semua", "APK", "Workspace", "DEX", "Resource")

    val displayedProjects = remember(selectedFilter, projectsList) {
        if (selectedFilter == "Semua") projectsList
        else projectsList.filter { it.type.equals(selectedFilter, ignoreCase = true) }
    }

    ScreenColumn(modifier = modifier) {
        GradientBanner(
            title = "Workspace Terpadu",
            subtitle = "Kelola proyek reverse engineering, decompile, patch bytecode, dan build APK dalam penyimpanan lokal terisolasi.",
            actionText = "+ Buat Proyek Baru",
            onActionClick = { showCreateDialog = true }
        )

        // Filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(filters) { f ->
                FilterChip(
                    selected = selectedFilter == f,
                    onClick = { selectedFilter = f },
                    label = { Text(f, fontSize = 11.sp, fontWeight = if (selectedFilter == f) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VioletPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        SectionTitle(title = "Daftar Proyek Aktif (${displayedProjects.size})")

        displayedProjects.forEach { project ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().testTag("project_item_${project.id}")
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (project.type) {
                                            "apk" -> EmeraldSuccess.copy(alpha = 0.15f)
                                            "workspace" -> VioletPrimary.copy(alpha = 0.15f)
                                            "dex" -> BlueSecondary.copy(alpha = 0.15f)
                                            else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (project.type) {
                                        "apk" -> Icons.Default.Android
                                        "workspace" -> Icons.Default.Workspaces
                                        "dex" -> Icons.Default.DataObject
                                        else -> Icons.Default.Folder
                                    },
                                    contentDescription = null,
                                    tint = when (project.type) {
                                        "apk" -> EmeraldSuccess
                                        "workspace" -> VioletPrimary
                                        "dex" -> BlueSecondary
                                        else -> Color(0xFFD97706)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(project.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Text(project.detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }

                        // Status badge
                        Surface(
                            shape = CircleShape,
                            color = when (project.status) {
                                "Ready" -> Color(0xFFD1FAE5)
                                "Signed" -> Color(0xFFDBEAFE)
                                "Decompiled" -> Color(0xFFEDE7FF)
                                else -> Color(0xFFFEF3C7)
                            }
                        ) {
                            Text(
                                text = project.status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (project.status) {
                                    "Ready" -> Color(0xFF065F46)
                                    "Signed" -> Color(0xFF1E40AF)
                                    "Decompiled" -> VioletPrimary
                                    else -> Color(0xFF92400E)
                                },
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Diperbarui: ${project.lastModified}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { onOpenProject(project) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Launch, null, tint = VioletPrimary, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    projectsList = projectsList.filter { it.id != project.id }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(60.dp))
    }

    if (showCreateDialog) {
        var newName by remember { mutableStateOf("") }
        var newPkg by remember { mutableStateOf("com.example.mod") }
        var newType by remember { mutableStateOf("apk") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Buat Proyek Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nama Proyek") },
                        placeholder = { Text("misal: TargetApp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newPkg,
                        onValueChange = { newPkg = it },
                        label = { Text("Nama Paket") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val newProj = AppProject(
                                id = "p_${System.currentTimeMillis()}",
                                name = "$newName.apk",
                                type = newType,
                                detail = "0 B • Proyek baru dibuat",
                                size = "0 B",
                                lastModified = "Baru saja",
                                status = "Ready"
                            )
                            projectsList = listOf(newProj) + projectsList
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text("Buat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
