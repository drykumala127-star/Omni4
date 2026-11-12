package com.omnifile.feature.filemanager.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omnifile.feature.filemanager.info.FileHashCalculator
import com.omnifile.feature.filemanager.info.FileHashes
import com.omnifile.feature.filemanager.model.*
import com.omnifile.feature.filemanager.repository.FileRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BreadcrumbItem(val title: String, val uri: Uri)

data class FileManagerUiState(
    val currentUri: Uri? = null,
    val currentPathDisplay: String = "Penyimpanan Perangkat",
    val breadcrumbs: List<BreadcrumbItem> = emptyList(),
    val items: List<FileEntry> = emptyList(),
    val selectedUris: Set<Uri> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val sortOrder: SortOrder = SortOrder(),
    val isSortSheetOpen: Boolean = false,
    val searchQuery: String = "",
    val activeCategoryFilter: FileCategoryFilter = FileCategoryFilter.ALL,
    val filterCriteria: FilterCriteria = FilterCriteria(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSafPermission: Boolean = false,
    // Favorites & Recent Files
    val favorites: List<BookmarkItem> = emptyList(),
    val favoriteUriStrings: Set<String> = emptySet(),
    val recentRecords: List<RecentAccessRecord> = emptyList(),
    // File Details & Hash
    val isFileInfoDialogOpen: Boolean = false,
    val fileInfoTargetEntry: FileEntry? = null,
    val currentFileHashes: FileHashes? = null,
    val isCalculatingHash: Boolean = false,
    // File operation dialogs
    val isDeleteConfirmationOpen: Boolean = false,
    val isRenameDialogOpen: Boolean = false,
    val renameTargetEntry: FileEntry? = null,
    val isCreateFolderDialogOpen: Boolean = false,
    val isCompressDialogOpen: Boolean = false,
    val isExtractDialogOpen: Boolean = false,
    val extractTargetEntry: FileEntry? = null,
    // Clipboard for Copy / Move
    val clipboardUris: Set<Uri> = emptySet(),
    val isMoveOperation: Boolean = false,
    // Active operation progress
    val activeOperationProgress: OperationProgress? = null,
    val lastOperationResult: FileOperationResult? = null
) {
    val selectedTotalSizeBytes: Long
        get() = items.filter { selectedUris.contains(it.uri) && !it.isDirectory }.sumOf { it.sizeBytes }

    val selectedCountDisplay: String
        get() = if (selectedTotalSizeBytes > 0) {
            "${selectedUris.size} terpilih (${FileEntry.formatBytes(selectedTotalSizeBytes, false)})"
        } else {
            "${selectedUris.size} terpilih"
        }
}

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepositoryImpl(application.applicationContext)

    private val _uiState = MutableStateFlow(FileManagerUiState())
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    private var activeOperationJob: Job? = null
    private var searchJob: Job? = null
    private var hashJob: Job? = null

    init {
        observeFavoritesAndRecents()
        checkPersistedPermissions()
    }

    private fun observeFavoritesAndRecents() {
        viewModelScope.launch {
            repository.getFavorites().collect { favList ->
                _uiState.update { state ->
                    state.copy(
                        favorites = favList,
                        favoriteUriStrings = favList.map { it.uriString }.toSet()
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.getRecentAccessRecords().collect { recents ->
                _uiState.update { it.copy(recentRecords = recents) }
            }
        }
    }

    private fun checkPersistedPermissions() {
        val persisted = repository.getPersistedUris()
        if (persisted.isNotEmpty()) {
            val rootUri = persisted.first()
            _uiState.update { it.copy(hasSafPermission = true) }
            navigateTo(rootUri, "Penyimpanan Utama", isRoot = true)
        }
    }

    fun onDirectoryGranted(treeUri: Uri) {
        repository.takePersistablePermission(treeUri)
        _uiState.update { it.copy(hasSafPermission = true) }
        navigateTo(treeUri, "Penyimpanan Terpilih", isRoot = true)
    }

    fun navigateTo(uri: Uri, displayName: String, isRoot: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { state ->
                val newBreadcrumbs = if (isRoot) {
                    listOf(BreadcrumbItem(displayName, uri))
                } else {
                    state.breadcrumbs + BreadcrumbItem(displayName, uri)
                }
                state.copy(
                    currentUri = uri,
                    currentPathDisplay = displayName,
                    breadcrumbs = newBreadcrumbs,
                    isLoading = true,
                    errorMessage = null,
                    selectedUris = emptySet(),
                    isMultiSelectMode = false,
                    searchQuery = ""
                )
            }
            loadItems()
        }
    }

    fun navigateUp(): Boolean {
        val currentBreadcrumbs = _uiState.value.breadcrumbs
        if (currentBreadcrumbs.size <= 1) {
            return false
        }
        val targetBreadcrumb = currentBreadcrumbs[currentBreadcrumbs.size - 2]
        val updatedBreadcrumbs = currentBreadcrumbs.dropLast(1)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentUri = targetBreadcrumb.uri,
                    currentPathDisplay = targetBreadcrumb.title,
                    breadcrumbs = updatedBreadcrumbs,
                    isLoading = true,
                    errorMessage = null,
                    selectedUris = emptySet(),
                    isMultiSelectMode = false,
                    searchQuery = ""
                )
            }
            loadItems()
        }
        return true
    }

    private suspend fun loadItems() {
        val current = _uiState.value.currentUri ?: return
        val criteria = _uiState.value.filterCriteria
        val order = _uiState.value.sortOrder

        val result = if (_uiState.value.searchQuery.isNotBlank()) {
            repository.search(_uiState.value.searchQuery, current, recursive = true, filterCriteria = criteria)
        } else {
            repository.listFiles(current, sortOrder = order, filterCriteria = criteria)
        }

        result.fold(
            onSuccess = { files ->
                _uiState.update { it.copy(items = files, isLoading = false) }
            },
            onFailure = { err ->
                _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "Gagal membaca file") }
            }
        )
    }

    fun refresh() {
        val currentUri = _uiState.value.currentUri ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loadItems()
        }
    }

    // --- File Details & Cryptographic Hashes ---

    fun openFileInfoDialog(entry: FileEntry) {
        _uiState.update {
            it.copy(
                isFileInfoDialogOpen = true,
                fileInfoTargetEntry = entry,
                currentFileHashes = null,
                isCalculatingHash = false
            )
        }
    }

    fun closeFileInfoDialog() {
        hashJob?.cancel()
        _uiState.update {
            it.copy(
                isFileInfoDialogOpen = false,
                fileInfoTargetEntry = null,
                currentFileHashes = null,
                isCalculatingHash = false
            )
        }
    }

    fun calculateHashesForCurrentInfo() {
        val entry = _uiState.value.fileInfoTargetEntry ?: return
        if (entry.isDirectory) return

        hashJob?.cancel()
        hashJob = viewModelScope.launch {
            _uiState.update { it.copy(isCalculatingHash = true) }
            val hashesResult = FileHashCalculator.computeHashes(
                getApplication<Application>().contentResolver,
                entry.uri
            )
            hashesResult.fold(
                onSuccess = { hashes ->
                    _uiState.update { it.copy(currentFileHashes = hashes, isCalculatingHash = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isCalculatingHash = false) }
                }
            )
        }
    }

    // --- Favorites & Recent Actions ---

    fun toggleFavorite(entry: FileEntry) {
        repository.toggleFavorite(entry.uri, entry.name, entry.isDirectory)
    }

    fun onFileInteracted(entry: FileEntry) {
        repository.recordRecentAccess(entry.uri, entry.name, entry.isDirectory, entry.sizeBytes)
    }

    // --- Search & Filtering ---

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            _uiState.update { it.copy(isLoading = true) }
            loadItems()
        }
    }

    fun onCategoryFilterSelect(filter: FileCategoryFilter) {
        val updatedCriteria = _uiState.value.filterCriteria.copy(categoryFilter = filter)
        _uiState.update {
            it.copy(activeCategoryFilter = filter, filterCriteria = updatedCriteria, isLoading = true)
        }
        viewModelScope.launch {
            loadItems()
        }
    }

    fun openSortSheet() {
        _uiState.update { it.copy(isSortSheetOpen = true) }
    }

    fun closeSortSheet() {
        _uiState.update { it.copy(isSortSheetOpen = false) }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order, isSortSheetOpen = false, isLoading = true) }
        viewModelScope.launch {
            loadItems()
        }
    }

    // --- Multi-Selection Actions ---

    fun toggleSelection(uri: Uri) {
        _uiState.update { state ->
            val updated = if (state.selectedUris.contains(uri)) {
                state.selectedUris - uri
            } else {
                state.selectedUris + uri
            }
            state.copy(
                selectedUris = updated,
                isMultiSelectMode = updated.isNotEmpty()
            )
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            val allUris = state.items.map { it.uri }.toSet()
            state.copy(selectedUris = allUris, isMultiSelectMode = allUris.isNotEmpty())
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedUris = emptySet(), isMultiSelectMode = false) }
    }

    // --- File Operations ---

    fun openCreateFolderDialog() {
        _uiState.update { it.copy(isCreateFolderDialogOpen = true) }
    }

    fun closeCreateFolderDialog() {
        _uiState.update { it.copy(isCreateFolderDialogOpen = false) }
    }

    fun createFolder(name: String) {
        val parent = _uiState.value.currentUri ?: return
        closeCreateFolderDialog()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.createFolder(parent, name)
            res.fold(
                onSuccess = { refresh() },
                onFailure = { err -> _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage) } }
            )
        }
    }

    fun openRenameDialog(entry: FileEntry) {
        _uiState.update { it.copy(isRenameDialogOpen = true, renameTargetEntry = entry) }
    }

    fun closeRenameDialog() {
        _uiState.update { it.copy(isRenameDialogOpen = false, renameTargetEntry = null) }
    }

    fun rename(newName: String) {
        val target = _uiState.value.renameTargetEntry ?: return
        closeRenameDialog()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.rename(target.uri, newName)
            res.fold(
                onSuccess = {
                    clearSelection()
                    refresh()
                },
                onFailure = { err -> _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage) } }
            )
        }
    }

    fun openDeleteConfirmation() {
        if (_uiState.value.selectedUris.isNotEmpty()) {
            _uiState.update { it.copy(isDeleteConfirmationOpen = true) }
        }
    }

    fun closeDeleteConfirmation() {
        _uiState.update { it.copy(isDeleteConfirmationOpen = false) }
    }

    fun executeDelete() {
        val targets = _uiState.value.selectedUris.toList()
        if (targets.isEmpty()) return
        closeDeleteConfirmation()

        activeOperationJob?.cancel()
        activeOperationJob = viewModelScope.launch {
            repository.delete(targets).collect { (progress, result) ->
                _uiState.update {
                    it.copy(
                        activeOperationProgress = if (result == null) progress else null,
                        lastOperationResult = result
                    )
                }
                if (result != null) {
                    clearSelection()
                    refresh()
                }
            }
        }
    }

    fun copySelected() {
        val selected = _uiState.value.selectedUris
        if (selected.isEmpty()) return
        _uiState.update {
            it.copy(
                clipboardUris = selected,
                isMoveOperation = false,
                isMultiSelectMode = false,
                selectedUris = emptySet()
            )
        }
    }

    fun cutSelected() {
        val selected = _uiState.value.selectedUris
        if (selected.isEmpty()) return
        _uiState.update {
            it.copy(
                clipboardUris = selected,
                isMoveOperation = true,
                isMultiSelectMode = false,
                selectedUris = emptySet()
            )
        }
    }

    fun pasteClipboard() {
        val targetParent = _uiState.value.currentUri ?: return
        val sources = _uiState.value.clipboardUris.toList()
        val isMove = _uiState.value.isMoveOperation
        if (sources.isEmpty()) return

        activeOperationJob?.cancel()
        activeOperationJob = viewModelScope.launch {
            val operationFlow = if (isMove) {
                repository.move(sources, targetParent, ConflictStrategy.AUTO_RENAME)
            } else {
                repository.copy(sources, targetParent, ConflictStrategy.AUTO_RENAME)
            }

            operationFlow.collect { (progress, result) ->
                _uiState.update {
                    it.copy(
                        activeOperationProgress = if (result == null) progress else null,
                        lastOperationResult = result
                    )
                }
                if (result != null) {
                    _uiState.update { it.copy(clipboardUris = emptySet()) }
                    refresh()
                }
            }
        }
    }

    // --- ZIP Compression & Extraction Operations ---

    fun openCompressDialog() {
        if (_uiState.value.selectedUris.isNotEmpty()) {
            _uiState.update { it.copy(isCompressDialogOpen = true) }
        }
    }

    fun closeCompressDialog() {
        _uiState.update { it.copy(isCompressDialogOpen = false) }
    }

    fun compressSelected(zipFileName: String) {
        val targetParent = _uiState.value.currentUri ?: return
        val sources = _uiState.value.selectedUris.toList()
        if (sources.isEmpty()) return
        closeCompressDialog()

        activeOperationJob?.cancel()
        activeOperationJob = viewModelScope.launch {
            repository.compressZip(sources, targetParent, zipFileName).collect { (progress, result) ->
                _uiState.update {
                    it.copy(
                        activeOperationProgress = if (result == null) progress else null,
                        lastOperationResult = result
                    )
                }
                if (result != null) {
                    clearSelection()
                    refresh()
                }
            }
        }
    }

    fun openExtractDialog(entry: FileEntry) {
        _uiState.update { it.copy(isExtractDialogOpen = true, extractTargetEntry = entry) }
    }

    fun closeExtractDialog() {
        _uiState.update { it.copy(isExtractDialogOpen = false, extractTargetEntry = null) }
    }

    fun extractZip(entry: FileEntry) {
        val currentDest = _uiState.value.currentUri ?: return
        closeExtractDialog()

        activeOperationJob?.cancel()
        activeOperationJob = viewModelScope.launch {
            repository.extractZip(entry.uri, currentDest).collect { (progress, result) ->
                _uiState.update {
                    it.copy(
                        activeOperationProgress = if (result == null) progress else null,
                        lastOperationResult = result
                    )
                }
                if (result != null) {
                    refresh()
                }
            }
        }
    }

    fun cancelActiveOperation() {
        activeOperationJob?.cancel()
        _uiState.update {
            it.copy(
                activeOperationProgress = null,
                lastOperationResult = FileOperationResult.Cancelled
            )
        }
        refresh()
    }
}
