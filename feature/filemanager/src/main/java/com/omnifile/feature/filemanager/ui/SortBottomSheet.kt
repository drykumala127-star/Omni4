package com.omnifile.feature.filemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnifile.feature.filemanager.model.SortDirection
import com.omnifile.feature.filemanager.model.SortField
import com.omnifile.feature.filemanager.model.SortOrder

private val Violet = Color(0xFF673DE6)
private val Ink = Color(0xFF17143A)
private val Muted = Color(0xFF77758D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Urutkan Berdasarkan",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            // Sort Fields Options
            val fields = listOf(
                SortField.NAME to "Nama File",
                SortField.SIZE to "Ukuran File",
                SortField.DATE to "Tanggal Modifikasi",
                SortField.TYPE to "Tipe / Ekstensi"
            )

            fields.forEach { (field, label) ->
                val isSelected = currentSortOrder.field == field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSortOrderChange(currentSortOrder.copy(field = field))
                        }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Violet else Ink
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Violet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F5))

            // Direction Selection (Ascending / Descending)
            Text(
                text = "Arah Pengurutan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onSortOrderChange(currentSortOrder.copy(direction = SortDirection.ASCENDING))
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (currentSortOrder.direction == SortDirection.ASCENDING) {
                        ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFEDE8FF), contentColor = Violet)
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Menaik (A-Z)")
                }

                OutlinedButton(
                    onClick = {
                        onSortOrderChange(currentSortOrder.copy(direction = SortDirection.DESCENDING))
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (currentSortOrder.direction == SortDirection.DESCENDING) {
                        ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFEDE8FF), contentColor = Violet)
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Menurun (Z-A)")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
