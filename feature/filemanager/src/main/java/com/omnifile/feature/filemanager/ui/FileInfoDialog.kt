package com.omnifile.feature.filemanager.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnifile.feature.filemanager.info.FileHashes
import com.omnifile.feature.filemanager.model.FileEntry

private val Violet = Color(0xFF673DE6)
private val Ink = Color(0xFF17143A)
private val Muted = Color(0xFF77758D)

@Composable
fun FileInfoDialog(
    entry: FileEntry,
    hashes: FileHashes?,
    isCalculatingHash: Boolean,
    onCalculateHashClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onOpenFileClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Violet, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Informasi Berkas", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoRow("Nama", entry.name)
                InfoRow("Ukuran", "${entry.formattedSize} (${entry.sizeBytes} bytes)")
                InfoRow("Tipe", entry.mimeType ?: "application/octet-stream")
                InfoRow("Diubah", entry.formattedDate)
                InfoRow("URI", entry.uri.toString(), copyable = true, context = context)

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Integritas & Hash Kriptografis",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Ink
                )

                if (entry.isDirectory) {
                    Text(
                        text = "Hash hanya dihitung untuk berkas, bukan direktori.",
                        fontSize = 11.sp,
                        color = Muted
                    )
                } else if (hashes != null) {
                    HashBox("MD5", hashes.md5, context)
                    HashBox("SHA-1", hashes.sha1, context)
                    HashBox("SHA-256", hashes.sha256, context)
                } else if (isCalculatingHash) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Violet)
                        Text("Menghitung checksum (MD5, SHA-256)...", fontSize = 11.sp, color = Muted)
                    }
                } else {
                    OutlinedButton(
                        onClick = onCalculateHashClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hitung Hash (MD5 / SHA-256)", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!entry.isDirectory) {
                    OutlinedButton(onClick = onShareClicked) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Bagikan")
                    }
                    Button(
                        onClick = onOpenFileClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = Violet)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Buka")
                    }
                } else {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Violet)) {
                        Text("Tutup")
                    }
                }
            }
        },
        dismissButton = {
            if (!entry.isDirectory) {
                TextButton(onClick = onDismiss) {
                    Text("Tutup")
                }
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String, copyable: Boolean = false, context: Context? = null) {
    Column {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Muted)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Ink,
                maxLines = if (copyable) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (copyable && context != null) {
                IconButton(
                    onClick = { copyToClipboard(context, label, value) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = Violet, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun HashBox(label: String, hash: String, context: Context) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF7F7FC),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { copyToClipboard(context, label, hash) }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Violet)
                Text(
                    text = hash,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = Muted, modifier = Modifier.size(14.dp))
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "$label disalin ke papan klip", Toast.LENGTH_SHORT).show()
}
