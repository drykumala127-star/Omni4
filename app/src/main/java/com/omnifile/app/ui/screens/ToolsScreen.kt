package com.omnifile.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnifile.app.ui.components.ScreenColumn
import com.omnifile.app.ui.theme.BlueSecondary
import com.omnifile.app.ui.theme.EmeraldSuccess
import com.omnifile.app.ui.theme.RoseAccent
import com.omnifile.app.ui.theme.VioletPrimary
import com.omnifile.app.ui.tools.ToolDefinition

val ALL_TOOLS = listOf(
    ToolDefinition("apktool", "APKTool", "Decode & rebuild APK", "reversing", VioletPrimary),
    ToolDefinition("jadx", "JADX", "DEX ke Java source", "reversing", BlueSecondary),
    ToolDefinition("apk-editor", "APK Editor", "Edit struktur APK", "editors", RoseAccent),
    ToolDefinition("dex-explorer", "DEX Explorer", "Jelajahi classes.dex", "reversing", VioletPrimary),
    ToolDefinition("smali-editor", "Smali Editor", "Lihat & edit Smali", "editors", BlueSecondary),
    ToolDefinition("xml-editor", "XML Editor", "Edit format XML", "editors", EmeraldSuccess),
    ToolDefinition("apk-signer", "APK Signer", "Tanda tangani APK", "utilities", Color(0xFFEE9A2C)),
    ToolDefinition("zip-tools", "ZIP Tools", "Buat & ekstrak arsip", "utilities", BlueSecondary),
    ToolDefinition("hash-tools", "Hash Tools", "MD5, SHA-1, SHA-256", "utilities", EmeraldSuccess),
    ToolDefinition("manifest-editor", "Manifest Editor", "Kelola manifest", "editors", VioletPrimary),
    ToolDefinition("sqlite-browser", "SQLite Browser", "Jelajahi database", "system", BlueSecondary),
    ToolDefinition("hex-editor", "Hex Editor", "Edit data hexadecimal", "utilities", RoseAccent)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    modifier: Modifier = Modifier,
    onOpenTool: (ToolDefinition) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val categories = listOf("Semua", "reversing", "editors", "system", "utilities")

    val filteredTools = remember(searchQuery, selectedCategory) {
        ALL_TOOLS.filter { tool ->
            val matchesSearch = tool.name.contains(searchQuery, ignoreCase = true) ||
                                tool.desc.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "Semua" || tool.category.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    ScreenColumn(modifier = modifier) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tools_search_field"),
            placeholder = { Text("Cari tools...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = VioletPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VioletPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        // Category filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = cat.replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VioletPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Tools 2-Column Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(720.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            userScrollEnabled = false
        ) {
            items(filteredTools) { tool ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("tool_card_${tool.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(126.dp)
                            .clickable { onOpenTool(tool) }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(tool.tint.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Build, null, tint = tool.tint, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = tool.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = tool.desc,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(60.dp))
    }
}
