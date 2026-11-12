package com.omnifile.feature.filemanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.omnifile.feature.filemanager.model.OperationProgress

private val Violet = Color(0xFF673DE6)

@Composable
fun ConfirmDeleteDialog(
    itemCount: Int,
    itemNames: List<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hapus File Permanen?",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (itemCount == 1) {
                        "Apakah Anda yakin ingin menghapus '${itemNames.firstOrNull() ?: "item ini"}'?"
                    } else {
                        "Apakah Anda yakin ingin menghapus $itemCount item terpilih?"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Perhatian: Tindakan ini tidak dapat dibatalkan.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Hapus", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun RenameDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    val isIllegal = text.contains(Regex("[/\\\\:*?\"<>|]")) || text.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti Nama", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text("Nama Baru") },
                    isError = isIllegal,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isIllegal) {
                    Text(
                        text = if (text.isBlank()) "Nama tidak boleh kosong" else "Karakter tidak valid: / \\ : * ? \" < > |",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isIllegal) onConfirm(text.trim()) },
                enabled = !isIllegal,
                colors = ButtonDefaults.buttonColors(containerColor = Violet)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun CreateFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    val isIllegal = folderName.contains(Regex("[/\\\\:*?\"<>|]")) || folderName.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Folder Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    singleLine = true,
                    placeholder = { Text("Nama folder...") },
                    label = { Text("Nama Folder") },
                    isError = isIllegal && folderName.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (folderName.contains(Regex("[/\\\\:*?\"<>|]"))) {
                    Text(
                        text = "Karakter dilarang: / \\ : * ? \" < > |",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (!isIllegal) onConfirm(folderName.trim()) },
                enabled = !isIllegal,
                colors = ButtonDefaults.buttonColors(containerColor = Violet)
            ) {
                Text("Buat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun CompressToZipDialog(
    defaultZipName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var zipName by remember { mutableStateOf(defaultZipName) }
    val isIllegal = zipName.contains(Regex("[/\\\\:*?\"<>|]")) || zipName.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kompres ke Berkas ZIP", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tentukan nama berkas arsip ZIP baru:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = zipName,
                    onValueChange = { zipName = it },
                    singleLine = true,
                    label = { Text("Nama Arsip (.zip)") },
                    isError = isIllegal,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isIllegal) {
                    Text(
                        text = if (zipName.isBlank()) "Nama arsip tidak boleh kosong" else "Karakter tidak valid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isIllegal) {
                        val finalName = if (zipName.endsWith(".zip")) zipName else "$zipName.zip"
                        onConfirm(finalName)
                    }
                },
                enabled = !isIllegal,
                colors = ButtonDefaults.buttonColors(containerColor = Violet)
            ) {
                Text("Kompres Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ExtractZipDialog(
    archiveName: String,
    targetFolderName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ekstrak Berkas ZIP", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ekstrak arsip '$archiveName' ke direktori saat ini?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Direktori tujuan: $targetFolderName",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Proteksi Zip Slip aktif: Berkas dengan path traversal berbahaya akan ditolak secara otomatis.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Violet,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Violet)
            ) {
                Text("Ekstrak")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun OperationProgressDialog(
    progress: OperationProgress,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (progress.operation.name) {
                        "COMPRESS_ZIP" -> "Mengompresi ke ZIP..."
                        "EXTRACT_ZIP" -> "Mengekstrak Arsip ZIP..."
                        else -> "Memproses ${progress.operation.name}..."
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (progress.isIndeterminate || (progress.totalItems <= 1 && progress.totalBytes <= 0)) {
                    CircularProgressIndicator(color = Violet, modifier = Modifier.size(48.dp))
                } else {
                    LinearProgressIndicator(
                        progress = { progress.progressFraction },
                        color = Violet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                    Text(
                        text = if (progress.totalBytes > 0) {
                            "${progress.bytesProcessed / 1024} KB / ${progress.totalBytes / 1024} KB (${(progress.progressFraction * 100).toInt()}%)"
                        } else {
                            "${progress.itemsProcessed} / ${progress.totalItems} item (${(progress.progressFraction * 100).toInt()}%)"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (progress.currentItemName.isNotBlank()) {
                    Text(
                        text = progress.currentItemName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                if (progress.isCancellable) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Batalkan")
                    }
                }
            }
        }
    }
}
