package com.omnifile.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omnifile.app.ui.components.AppTopBar
import com.omnifile.app.ui.dialogs.EngineTerminalDialog
import com.omnifile.app.ui.dialogs.ProductionChecklistDialog
import com.omnifile.app.ui.dialogs.RoadmapStagesDialog
import com.omnifile.app.ui.screens.*
import com.omnifile.app.ui.theme.*
import com.omnifile.app.ui.tools.ToolDefinition
import com.omnifile.app.ui.tools.ToolDialog
import com.omnifile.feature.filemanager.ui.*
import com.omnifile.feature.filemanager.viewmodel.FileManagerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OmniFileApp()
        }
    }
}

private data class TabItem(val titleId: String, val titleEn: String, val icon: ImageVector, val tag: String)

private val tabs = listOf(
    TabItem("Beranda", "Home", Icons.Default.Home, "tab_home"),
    TabItem("Tools", "Tools", Icons.Default.Build, "tab_tools"),
    TabItem("Files", "Files", Icons.Default.Folder, "tab_files"),
    TabItem("Proyek", "Projects", Icons.Default.Workspaces, "tab_projects"),
    TabItem("Lainnya", "More", Icons.Default.GridView, "tab_more")
)

@Composable
private fun OmniFileApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentTheme by remember { mutableStateOf(AppThemeMode.VIOLET) }
    var currentLang by remember { mutableStateOf("id") }

    // Dialog state
    var selectedTool by remember { mutableStateOf<ToolDefinition?>(null) }
    var isTerminalOpen by remember { mutableStateOf(false) }
    var isRoadmapOpen by remember { mutableStateOf(false) }
    var isHardeningOpen by remember { mutableStateOf(false) }

    // Handle back button: close modals first, or return to home tab
    BackHandler(enabled = selectedTool != null || isTerminalOpen || isRoadmapOpen || isHardeningOpen || selectedTab != 0) {
        when {
            selectedTool != null -> selectedTool = null
            isTerminalOpen -> isTerminalOpen = false
            isRoadmapOpen -> isRoadmapOpen = false
            isHardeningOpen -> isHardeningOpen = false
            selectedTab != 0 -> selectedTab = 0
        }
    }

    OmniFileTheme(themeMode = currentTheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AppTopBar(
                    title = "OmniFile",
                    subtitle = when (selectedTab) {
                        0 -> if (currentLang == "id") "File Manager & Android Toolkit" else "File Manager & Android Toolkit"
                        1 -> if (currentLang == "id") "12 Alat Reverse Engineering" else "12 Reverse Engineering Tools"
                        2 -> if (currentLang == "id") "Penyimpanan Berkas Nyata & SAF" else "Real File Storage & SAF"
                        3 -> if (currentLang == "id") "Workspace Manajemen Proyek" else "Project Management Workspace"
                        else -> if (currentLang == "id") "Pengaturan Sistem & Privasi" else "System Settings & Privacy"
                    },
                    onOpenTerminal = { isTerminalOpen = true },
                    onOpenRoadmap = { isRoadmapOpen = true },
                    onOpenHardening = { isHardeningOpen = true },
                    currentLang = currentLang,
                    onToggleLang = { currentLang = if (currentLang == "id") "en" else "id" }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding().testTag("bottom_nav_bar")
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val tabLabel = if (currentLang == "id") tab.titleId else tab.titleEn
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(tab.icon, contentDescription = tabLabel) },
                            label = { Text(tabLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag(tab.tag),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        onNavigateToTab = { selectedTab = it },
                        onOpenTool = { selectedTool = it }
                    )
                    1 -> ToolsScreen(
                        onOpenTool = { selectedTool = it }
                    )
                    2 -> FileManagerTab()
                    3 -> ProjectsScreen(
                        onOpenProject = { proj ->
                            selectedTool = ToolDefinition(
                                id = "apktool",
                                name = "APKTool: ${proj.name}",
                                desc = "Analisis dan modifikasi proyek ${proj.name}",
                                category = "reversing",
                                tint = VioletPrimary
                            )
                        }
                    )
                    else -> MoreScreen(
                        currentTheme = currentTheme,
                        onSelectTheme = { currentTheme = it },
                        currentLang = currentLang,
                        onToggleLang = { currentLang = if (currentLang == "id") "en" else "id" },
                        onOpenTerminal = { isTerminalOpen = true },
                        onOpenHardening = { isHardeningOpen = true },
                        onOpenRoadmap = { isRoadmapOpen = true }
                    )
                }
            }
        }

        // Tool Modal Dialog
        selectedTool?.let { tool ->
            ToolDialog(
                tool = tool,
                onDismiss = { selectedTool = null }
            )
        }

        // Terminal CLI Modal
        if (isTerminalOpen) {
            EngineTerminalDialog(onDismiss = { isTerminalOpen = false })
        }

        // Hardening Checklist Modal
        if (isHardeningOpen) {
            ProductionChecklistDialog(onDismiss = { isHardeningOpen = false })
        }

        // Roadmap Stages Modal
        if (isRoadmapOpen) {
            RoadmapStagesDialog(onDismiss = { isRoadmapOpen = false })
        }
    }
}

@Composable
private fun FileManagerTab(
    fileManagerViewModel: FileManagerViewModel = viewModel()
) {
    val uiState by fileManagerViewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri != null) {
            fileManagerViewModel.onDirectoryGranted(treeUri)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Storage Header Card
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Penyimpanan Perangkat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.hasSafPermission) "Akses SAF Terverifikasi (Persistable)" else "Pilih Folder Melalui SAF",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { safLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (uiState.hasSafPermission) "Ganti Folder" else "Pilih Folder",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (!uiState.hasSafPermission) {
                StoragePermissionPrompt(
                    onGrantClicked = { safLauncher.launch(null) }
                )
            } else {
                if (uiState.favorites.isNotEmpty()) {
                    FavoritesQuickBar(
                        favorites = uiState.favorites,
                        onBookmarkClicked = { bookmark ->
                            fileManagerViewModel.navigateTo(bookmark.uri, bookmark.name)
                        }
                    )
                }

                SearchFilterBar(
                    searchQuery = uiState.searchQuery,
                    onQueryChange = { fileManagerViewModel.onSearchQueryChange(it) },
                    activeFilter = uiState.activeCategoryFilter,
                    onFilterSelect = { fileManagerViewModel.onCategoryFilterSelect(it) },
                    onSortClick = { fileManagerViewModel.openSortSheet() }
                )

                if (uiState.breadcrumbs.isNotEmpty() && uiState.searchQuery.isBlank()) {
                    BreadcrumbBar(
                        breadcrumbs = uiState.breadcrumbs,
                        onBreadcrumbClicked = { crumb ->
                            fileManagerViewModel.navigateTo(crumb.uri, crumb.title)
                        }
                    )
                }

                // Controls row
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    color = if (uiState.isMultiSelectMode) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isMultiSelectMode) uiState.selectedCountDisplay else "Daftar Berkas (${uiState.items.size})",
                            color = if (uiState.isMultiSelectMode) VioletPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            if (uiState.isMultiSelectMode) {
                                TextButton(onClick = { fileManagerViewModel.selectAll() }) {
                                    Text("Pilih Semua", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { fileManagerViewModel.clearSelection() }) {
                                    Text("Batal", fontSize = 11.sp)
                                }
                            } else {
                                IconButton(onClick = { fileManagerViewModel.openCreateFolderDialog() }) {
                                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Folder Baru", tint = VioletPrimary)
                                }
                                IconButton(onClick = { fileManagerViewModel.refresh() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = VioletPrimary)
                                }
                            }
                        }
                    }
                }

                // Multi-select Action Bar
                if (uiState.isMultiSelectMode) {
                    val singleSelectedFile = if (uiState.selectedUris.size == 1) {
                        uiState.items.firstOrNull { uiState.selectedUris.contains(it.uri) }
                    } else null

                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { fileManagerViewModel.copySelected() }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = VioletPrimary)
                            }
                            IconButton(onClick = { fileManagerViewModel.cutSelected() }) {
                                Icon(Icons.Default.ContentCut, contentDescription = "Pindah", tint = VioletPrimary)
                            }
                            IconButton(onClick = { fileManagerViewModel.openCompressDialog() }) {
                                Icon(Icons.Default.Archive, contentDescription = "Kompres ZIP", tint = VioletPrimary)
                            }
                            if (singleSelectedFile != null) {
                                IconButton(onClick = { fileManagerViewModel.openFileInfoDialog(singleSelectedFile) }) {
                                    Icon(Icons.Default.Info, contentDescription = "Info & Hash", tint = VioletPrimary)
                                }
                                IconButton(onClick = { fileManagerViewModel.openRenameDialog(singleSelectedFile) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Ganti Nama", tint = VioletPrimary)
                                }
                            }
                            IconButton(onClick = { fileManagerViewModel.openDeleteConfirmation() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Clipboard Paste Bar
                if (uiState.clipboardUris.isNotEmpty()) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${uiState.clipboardUris.size} item (${if (uiState.isMoveOperation) "Pindah" else "Salin"})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                            }
                            Button(
                                onClick = { fileManagerViewModel.pasteClipboard() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Tempel di Sini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VioletPrimary)
                    }
                } else if (uiState.items.isEmpty()) {
                    EmptyFolderView(
                        message = uiState.errorMessage ?: if (uiState.searchQuery.isNotBlank()) "Tidak ada berkas yang cocok dengan '${uiState.searchQuery}'" else "Folder ini kosong atau belum memiliki berkas."
                    )
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.items) { file ->
                            val isArchive = file.name.endsWith(".zip", true) || file.name.endsWith(".apk", true)
                            FileItemRow(
                                entry = file,
                                isSelected = uiState.selectedUris.contains(file.uri),
                                isFavorite = uiState.favoriteUriStrings.contains(file.uri.toString()),
                                onClick = {
                                    if (uiState.isMultiSelectMode) {
                                        fileManagerViewModel.toggleSelection(file.uri)
                                    } else if (file.isDirectory) {
                                        fileManagerViewModel.navigateTo(file.uri, file.name)
                                    } else {
                                        fileManagerViewModel.openFileInfoDialog(file)
                                    }
                                },
                                onLongClick = {
                                    fileManagerViewModel.toggleSelection(file.uri)
                                },
                                onToggleFavorite = {
                                    fileManagerViewModel.toggleFavorite(file)
                                },
                                onExtractZipClicked = if (isArchive) {
                                    { fileManagerViewModel.openExtractDialog(file) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // Operation Dialogs
        if (uiState.isDeleteConfirmationOpen) {
            val selectedNames = uiState.items
                .filter { uiState.selectedUris.contains(it.uri) }
                .map { it.name }

            ConfirmDeleteDialog(
                itemCount = uiState.selectedUris.size,
                itemNames = selectedNames,
                onConfirm = { fileManagerViewModel.executeDelete() },
                onDismiss = { fileManagerViewModel.closeDeleteConfirmation() }
            )
        }

        if (uiState.isRenameDialogOpen && uiState.renameTargetEntry != null) {
            RenameDialog(
                initialName = uiState.renameTargetEntry!!.name,
                onConfirm = { newName -> fileManagerViewModel.rename(newName) },
                onDismiss = { fileManagerViewModel.closeRenameDialog() }
            )
        }

        if (uiState.isCreateFolderDialogOpen) {
            CreateFolderDialog(
                onConfirm = { folderName -> fileManagerViewModel.createFolder(folderName) },
                onDismiss = { fileManagerViewModel.closeCreateFolderDialog() }
            )
        }

        if (uiState.isCompressDialogOpen) {
            CompressToZipDialog(
                defaultZipName = "Archive_${System.currentTimeMillis() / 1000}.zip",
                onConfirm = { zipName -> fileManagerViewModel.compressSelected(zipName) },
                onDismiss = { fileManagerViewModel.closeCompressDialog() }
            )
        }

        if (uiState.isExtractDialogOpen && uiState.extractTargetEntry != null) {
            ExtractZipDialog(
                archiveName = uiState.extractTargetEntry!!.name,
                targetFolderName = uiState.currentPathDisplay,
                onConfirm = { fileManagerViewModel.extractZip(uiState.extractTargetEntry!!) },
                onDismiss = { fileManagerViewModel.closeExtractDialog() }
            )
        }

        if (uiState.isFileInfoDialogOpen && uiState.fileInfoTargetEntry != null) {
            FileInfoDialog(
                entry = uiState.fileInfoTargetEntry!!,
                hashes = uiState.currentFileHashes,
                isCalculatingHash = uiState.isCalculatingHash,
                onCalculateHashClicked = { fileManagerViewModel.calculateHashesForCurrentInfo() },
                onShareClicked = {
                    val target = uiState.fileInfoTargetEntry!!
                    val shareIntent = com.omnifile.feature.filemanager.info.FileShareHelper.createShareIntent(
                        target.uri,
                        target.mimeType,
                        target.name
                    )
                    context.startActivity(shareIntent)
                },
                onOpenFileClicked = {
                    val target = uiState.fileInfoTargetEntry!!
                    try {
                        val openIntent = com.omnifile.feature.filemanager.info.FileShareHelper.createOpenFileIntent(
                            target.uri,
                            target.mimeType
                        )
                        context.startActivity(openIntent)
                    } catch (_: Exception) {}
                },
                onDismiss = { fileManagerViewModel.closeFileInfoDialog() }
            )
        }

        if (uiState.isSortSheetOpen) {
            SortBottomSheet(
                currentSortOrder = uiState.sortOrder,
                onSortOrderChange = { fileManagerViewModel.setSortOrder(it) },
                onDismiss = { fileManagerViewModel.closeSortSheet() }
            )
        }

        uiState.activeOperationProgress?.let { progress ->
            OperationProgressDialog(
                progress = progress,
                onCancel = { fileManagerViewModel.cancelActiveOperation() }
            )
        }
    }
}
