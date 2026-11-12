package com.omnifile.feature.filemanager.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnifile.feature.filemanager.model.FileCategoryFilter

private val Violet = Color(0xFF673DE6)

@Composable
fun SearchFilterBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    activeFilter: FileCategoryFilter,
    onFilterSelect: (FileCategoryFilter) -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Input Row with Sort Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Cari file atau folder...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Violet, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Violet,
                    unfocusedBorderColor = Color(0xFFE4E4EB)
                )
            )

            FilledTonalIconButton(
                onClick = onSortClick,
                shape = RoundedCornerShape(14.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFFEDE8FF),
                    contentColor = Violet
                )
            ) {
                Icon(Icons.Default.Sort, contentDescription = "Sort Files", modifier = Modifier.size(20.dp))
            }
        }

        // Category Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileCategoryFilter.values().forEach { filter ->
                val isSelected = activeFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelect(filter) },
                    label = {
                        Text(
                            text = filter.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Violet,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}
