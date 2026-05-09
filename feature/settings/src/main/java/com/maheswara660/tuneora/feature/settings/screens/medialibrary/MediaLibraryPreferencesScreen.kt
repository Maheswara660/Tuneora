package com.maheswara660.tuneora.feature.settings.screens.medialibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.feature.settings.viewmodels.MediaLibraryPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaLibraryPreferencesScreen(
    onNavigateUp: () -> Unit,
    onManageFoldersClick: () -> Unit = {},
    viewModel: MediaLibraryPreferencesViewModel = hiltViewModel(),
) {
    val preferences by viewModel.uiState.collectAsStateWithLifecycle()

    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = { Text("Media Library") },
                
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            ListSectionTitle(text = "Library Management")
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PreferenceSwitch(
                    title = "Mark Last Played",
                    description = "Show an indicator on the last played track",
                    icon = Icons.Rounded.Check,
                    isChecked = preferences.markLastPlayedMedia,
                    onClick = { viewModel.toggleMarkLastPlayedMedia() },
                    isFirstItem = true,
                    isLastItem = false
                )
                ClickablePreferenceItem(
                    title = "Manage Folders",
                    description = "Select which folders to scan for music",
                    icon = Icons.Rounded.FolderOpen,
                    onClick = onManageFoldersClick,
                    isFirstItem = false,
                    isLastItem = true
                )
            }
        }
    }
}
