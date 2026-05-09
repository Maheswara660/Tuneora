package com.maheswara660.tuneora.feature.settings.screens.general

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.feature.settings.viewmodels.GeneralPreferencesViewModel
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.rounded.DeleteForever

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralPreferencesScreen(
    onNavigateUp: () -> Unit,
    viewModel: GeneralPreferencesViewModel = hiltViewModel(),
) {
    val preferences by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }
    var showFastDeleteInfo by remember { mutableStateOf(false) }
    val context = LocalContext.current

    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = { Text("General") },
                
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
            ListSectionTitle(text = "App Settings")
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ClickablePreferenceItem(
                    title = "Fast Delete",
                    description = "Delete songs without system confirmation popups",
                    icon = Icons.Rounded.DeleteForever,
                    onClick = { showFastDeleteInfo = true },
                    isFirstItem = true
                )

                ClickablePreferenceItem(
                    title = "Reset Settings",
                    description = "Reset all app preferences to their default values",
                    icon = Icons.Rounded.History,
                    onClick = { showResetDialog = true },
                    isLastItem = true
                )
            }
        }

        if (showFastDeleteInfo) {
            TuneoraBottomSheet(
                onDismissRequest = { showFastDeleteInfo = false },
                title = "Fast Delete",
                confirmButton = {
                    Button(
                        onClick = {
                            val enabled = !preferences.fastDelete
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } else {
                                viewModel.updateFastDelete(enabled)
                            }
                            showFastDeleteInfo = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (preferences.fastDelete) "Disable" else "Enable")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showFastDeleteInfo = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = "Fast Delete allows you to delete songs directly from your device without the system confirmation popup.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "On Android 11+, this requires the 'All Files Access' permission.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Status: ${if (preferences.fastDelete) "Enabled" else "Disabled"}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (preferences.fastDelete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (showResetDialog) {
            TuneoraBottomSheet(
                onDismissRequest = { showResetDialog = false },
                title = "Reset Settings",
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetSettings()
                            showResetDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showResetDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                Text(
                    text = "Are you sure you want to reset all settings to default? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}
