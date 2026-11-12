package com.omnifile.feature.filemanager.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnifile.feature.filemanager.model.BookmarkItem
import com.omnifile.feature.filemanager.model.FileCategory
import com.omnifile.feature.filemanager.model.FileEntry
import com.omnifile.feature.filemanager.viewmodel.BreadcrumbItem

private val Violet = Color(0xFF673DE6)
private val Ink = Color(0xFF17143A)
private val Muted = Color(0xFF77758D)
private val Gold = Color(0xFFFFB300)

@Composable
fun BreadcrumbBar(
    breadcrumbs: List<BreadcrumbItem>,
    onBreadcrumbClicked: (BreadcrumbItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        breadcrumbs.forEachIndexed { index, crumb ->
            val isLast = index == breadcrumbs.size - 1
            Text(
                text = crumb.title,
                color = if (isLast) Violet else Muted,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isLast) { onBreadcrumbClicked(crumb) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
            if (!isLast) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FavoritesQuickBar(
    favorites: List<BookmarkItem>,
    onBookmarkClicked: (BookmarkItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (favorites.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Favorit Tersemat",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            favorites.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.clickable { onBookmarkClicked(item) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = if (item.isDirectory) Color(0xFFFFA126) else Violet,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemRow(
    entry: FileEntry,
    isSelected: Boolean,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: (() -> Unit)? = null,
    onExtractZipClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = getCategoryVisuals(entry)
    val isArchive = entry.category == FileCategory.ARCHIVE_ZIP || entry.category == FileCategory.ARCHIVE_APK

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFEDE8FF) else Color.White,
        shadowElevation = if (isSelected) 2.dp else 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${entry.formattedSize}  ·  ${entry.formattedDate}",
                    color = Muted,
                    fontSize = 10.sp
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Violet,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onToggleFavorite != null) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorit",
                                tint = if (isFavorite) Gold else Muted.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (isArchive && onExtractZipClicked != null) {
                        IconButton(
                            onClick = onExtractZipClicked,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Unarchive,
                                contentDescription = "Ekstrak ZIP",
                                tint = Violet,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultiSelectBottomActionBar(
    selectedCount: Int,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onCompressZip: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Batal", tint = Muted)
                }
                Text(
                    text = "$selectedCount dipilih",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Ink
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = Violet)
                }
                IconButton(onClick = onCut) {
                    Icon(Icons.Default.ContentCut, contentDescription = "Pindah", tint = Violet)
                }
                IconButton(onClick = onCompressZip) {
                    Icon(Icons.Default.Archive, contentDescription = "Kompres ZIP", tint = Color(0xFF168BFF))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun getCategoryVisuals(entry: FileEntry): Pair<ImageVector, Color> {
    if (entry.isDirectory) {
        return Icons.Default.Folder to Color(0xFFFFA126)
    }
    return when (entry.category) {
        FileCategory.ARCHIVE_APK -> Icons.Default.Android to Color(0xFF18A86B)
        FileCategory.ARCHIVE_ZIP -> Icons.Default.FolderZip to Color(0xFF168BFF)
        FileCategory.CODE_SOURCE -> Icons.Default.Code to Color(0xFF673DE6)
        FileCategory.IMAGE -> Icons.Default.Image to Color(0xFFE95A7B)
        FileCategory.DOCUMENT -> Icons.Default.Description to Color(0xFF3F51B5)
        FileCategory.AUDIO -> Icons.Default.AudioFile to Color(0xFF009688)
        FileCategory.VIDEO -> Icons.Default.VideoFile to Color(0xFFFF5722)
        FileCategory.UNKNOWN -> Icons.Default.InsertDriveFile to Color(0xFF77758D)
    }
}

@Composable
fun EmptyFolderView(message: String = "Folder ini kosong", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = Muted.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(text = message, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StoragePermissionPrompt(onGrantClicked: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF3F0FF),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Violet,
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Pilih Folder Penyimpanan",
                color = Ink,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Gunakan Android Storage Access Framework (SAF) untuk memilih folder yang diizinkan (misal folder Download atau Proyek).",
                color = Muted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onGrantClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Violet),
                shape = CircleShape
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Pilih Folder Melalui SAF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
