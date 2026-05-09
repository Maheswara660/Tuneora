package com.maheswara660.tuneora.feature.settings.screens.decoder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.common.model.DecoderPriority
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.feature.settings.viewmodels.DecoderPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoderPreferencesScreen(
    onNavigateUp: () -> Unit,
    viewModel: DecoderPreferencesViewModel = hiltViewModel(),
) {
    val preferences by viewModel.uiState.collectAsStateWithLifecycle()
    var showPriorityDialog by remember { mutableStateOf(false) }

    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = { Text("Decoder") },
                
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
            ListSectionTitle(text = "Audio Decoding")
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ClickablePreferenceItem(
                    title = "Decoder Priority",
                    description = when(preferences.decoderPriority) {
                        DecoderPriority.HARDWARE_FIRST -> "Hardware First"
                        DecoderPriority.SOFTWARE_FIRST -> "Software First"
                    },
                    icon = Icons.Rounded.PriorityHigh,
                    onClick = { showPriorityDialog = true },
                    isFirstItem = true,
                    isLastItem = true
                )
            }
        }

        if (showPriorityDialog) {
            TuneoraBottomSheet(
                onDismissRequest = { showPriorityDialog = false },
                title = "Decoder Priority",
                confirmButton = {
                    Button(
                        onClick = { showPriorityDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply")
                    }
                }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DecoderPriority.entries.forEach { priority ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = when(priority) {
                                    DecoderPriority.HARDWARE_FIRST -> "Hardware First"
                                    DecoderPriority.SOFTWARE_FIRST -> "Software First"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            RadioButton(
                                selected = preferences.decoderPriority == priority,
                                onClick = {
                                    viewModel.updateDecoderPriority(priority)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
