package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import com.maheswara660.tuneora.core.common.model.Sort
import com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsBottomSheet(
    preferences: ApplicationPreferences,
    onDismissRequest: () -> Unit,
    onUpdatePreferences: (ApplicationPreferences) -> Unit,
    onRefreshLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAppearance: () -> Unit
) {
    var currentPrefs by remember { mutableStateOf(preferences) }

    TuneoraBottomSheet(
        onDismissRequest = onDismissRequest,
        title = "Quick Settings",
        confirmButton = {
            Button(
                onClick = {
                    onUpdatePreferences(currentPrefs)
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                Column {
                    DialogSectionTitle(text = "Sort by")
                    SortOptions(
                        selectedSortBy = currentPrefs.sortBy,
                        onOptionSelected = { currentPrefs = currentPrefs.copy(sortBy = it) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Sort.Order.entries.forEachIndexed { index, order ->
                            SegmentedButton(
                                selected = currentPrefs.sortOrder == order,
                                onClick = { currentPrefs = currentPrefs.copy(sortOrder = order) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = Sort.Order.entries.size),
                                icon = {
                                    Icon(
                                        imageVector = if (order == Sort.Order.ASCENDING) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            ) {
                                Text(if (order == Sort.Order.ASCENDING) "Ascending" else "Descending")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            ListSectionTitle(text = "Refresh", contentPadding = PaddingValues(start = 16.dp, top = 8.dp, bottom = 8.dp))
            
            TuneoraSegmentedListItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { 
                    onRefreshLibrary()
                    onDismissRequest()
                },
                leadingContent = { Icon(Icons.Rounded.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                content = { Text("Rescan library") },
                isFirstItem = true,
                isLastItem = true
            )
        }
    }
}


@Composable
private fun SortOptions(
    selectedSortBy: Sort.By,
    onOptionSelected: (Sort.By) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SortOptionChip(
            text = "Title",
            icon = Icons.Rounded.Title,
            selected = selectedSortBy == Sort.By.TITLE,
            onClick = { onOptionSelected(Sort.By.TITLE) }
        )
        SortOptionChip(
            text = "Artist",
            icon = Icons.Rounded.Person,
            selected = selectedSortBy == Sort.By.ARTIST,
            onClick = { onOptionSelected(Sort.By.ARTIST) }
        )
        SortOptionChip(
            text = "Album",
            icon = Icons.Rounded.Album,
            selected = selectedSortBy == Sort.By.ALBUM,
            onClick = { onOptionSelected(Sort.By.ALBUM) }
        )
        SortOptionChip(
            text = "Date",
            icon = Icons.Rounded.CalendarMonth,
            selected = selectedSortBy == Sort.By.DATE,
            onClick = { onOptionSelected(Sort.By.DATE) }
        )
        SortOptionChip(
            text = "Size",
            icon = Icons.AutoMirrored.Rounded.CompareArrows,
            selected = selectedSortBy == Sort.By.SIZE,
            onClick = { onOptionSelected(Sort.By.SIZE) }
        )
    }
}

@Composable
private fun SortOptionChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

@Composable
private fun DialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
