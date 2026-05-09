package com.maheswara660.tuneora.feature.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.ui.components.TuneoraEmptyState
import com.maheswara660.tuneora.core.ui.components.TuneoraScreen
import com.maheswara660.tuneora.core.ui.components.TuneoraTopAppBar
import com.maheswara660.tuneora.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlacklistScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val blacklistedFolders = uiState.preferences.blacklistedFolders.toList()

    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = { Text("Blacklisted Folders") },
                
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (blacklistedFolders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                TuneoraEmptyState(
                    icon = Icons.Rounded.FolderOff,
                    title = "No blacklisted folders",
                    description = "Folders you blacklist will appear here."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(blacklistedFolders) { folder ->
                    ListItem(
                        headlineContent = { Text(folder) },
                        trailingContent = {
                            IconButton(onClick = {
                                viewModel.updatePreferences { it.copy(blacklistedFolders = it.blacklistedFolders - folder) }
                            }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Remove")
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
