package com.maheswara660.tuneora.feature.settings.screens.audio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.feature.settings.viewmodels.AudioPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPreferencesScreen(
    onNavigateUp: () -> Unit,
    viewModel: AudioPreferencesViewModel = hiltViewModel(),
) {
    val preferences by viewModel.uiState.collectAsStateWithLifecycle()

    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = { Text("Audio") },
                
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
            ListSectionTitle(text = "Playback Behavior")
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PreferenceSwitch(
                    title = "Require Audio Focus",
                    description = "Pause playback when another app plays audio",
                    icon = Icons.Rounded.Headset,
                    isChecked = preferences.requireAudioFocus,
                    onClick = { viewModel.toggleRequireAudioFocus() },
                    isFirstItem = true
                )
                PreferenceSwitch(
                    title = "Pause on Headset Disconnect",
                    description = "Automatically pause when headphones are removed",
                    icon = Icons.Rounded.HeadsetOff,
                    isChecked = preferences.pauseOnHeadsetDisconnect,
                    onClick = { viewModel.togglePauseOnHeadsetDisconnect() }
                )
                PreferenceSwitch(
                    title = "System Volume Panel",
                    description = "Show system volume controls when adjusting volume",
                    icon = Icons.AutoMirrored.Rounded.VolumeUp,
                    isChecked = preferences.showSystemVolumePanel,
                    onClick = { viewModel.toggleShowSystemVolumePanel() }
                )
                PreferenceSwitch(
                    title = "Volume Boost",
                    description = "Allow increasing volume beyond 100%",
                    icon = Icons.Rounded.Equalizer,
                    isChecked = preferences.enableVolumeBoost,
                    onClick = { viewModel.toggleVolumeBoost() },
                    isLastItem = true
                )
            }
        }
    }
}
