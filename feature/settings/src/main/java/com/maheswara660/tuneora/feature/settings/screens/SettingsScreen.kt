package com.maheswara660.tuneora.feature.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsInputComponent
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onAppearanceClick: () -> Unit = {},
    onPlaybackClick: () -> Unit = {},
    onMediaLibraryClick: () -> Unit = {},
    onDecoderClick: () -> Unit = {},
    onAudioClick: () -> Unit = {},
    onGeneralClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
) {
    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = { Text("Settings") },
                
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                ClickablePreferenceItem(
                    onClick = onAppearanceClick,
                    icon = Icons.Rounded.Palette,
                    title = "Appearance",
                    description = "Themes, accents, and more",
                    isFirstItem = true
                )
            }
            item {
                ClickablePreferenceItem(
                    onClick = onMediaLibraryClick,
                    icon = Icons.Rounded.Storage,
                    title = "Media Library",
                    description = "Folders, scanning, and management"
                )
            }
            item {
                ClickablePreferenceItem(
                    onClick = onPlaybackClick,
                    icon = Icons.Rounded.PlayCircle,
                    title = "Playback",
                    description = "Sleep timer, skip silence, and enhancements"
                )
            }
            item {
                ClickablePreferenceItem(
                    onClick = onAudioClick,
                    icon = Icons.Rounded.Equalizer,
                    title = "Audio",
                    description = "Focus, headset, and volume boost"
                )
            }
            item {
                ClickablePreferenceItem(
                    onClick = onDecoderClick,
                    icon = Icons.Rounded.SettingsInputComponent,
                    title = "Decoder",
                    description = "Hardware and software priority"
                )
            }
            item {
                ClickablePreferenceItem(
                    onClick = onGeneralClick,
                    icon = Icons.Rounded.Settings,
                    title = "General",
                    description = "App behavior and reset settings"
                )
            }
            item {
                ClickablePreferenceItem(
                    onClick = onAboutClick,
                    icon = Icons.Rounded.Info,
                    title = "About",
                    description = "App version, licenses, and info",
                    isLastItem = true
                )
            }
        }
    }
}
