package com.maheswara660.tuneora.feature.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.ui.components.ClickablePreferenceItem
import com.maheswara660.tuneora.core.ui.components.ListSectionTitle
import com.maheswara660.tuneora.core.ui.components.PreferenceSwitch
import com.maheswara660.tuneora.core.ui.components.PreferenceSlider
import com.maheswara660.tuneora.core.ui.components.TuneoraSegmentedListItem
import com.maheswara660.tuneora.core.ui.components.TuneoraScreen
import com.maheswara660.tuneora.core.ui.components.TuneoraTopAppBar
import com.maheswara660.tuneora.core.ui.components.TuneoraBottomSheet
import com.maheswara660.tuneora.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {},
    onNavigateToBlacklist: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = { Text("Playback") },
                
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )

        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
                    item {
                        ListSectionTitle(text = "Audio Engine", contentPadding = PaddingValues(start = 8.dp, bottom = 8.dp))
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Playback Tempo: ${uiState.preferences.playbackSpeed}x", style = MaterialTheme.typography.labelLarge)
                                Slider(
                                    value = uiState.preferences.playbackSpeed,
                                    onValueChange = { value -> 
                                        viewModel.updatePreferences { it.copy(playbackSpeed = value) }
                                    },
                                    valueRange = 0.5f..2.0f,
                                    steps = 14
                                )
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Audio Pitch: ${uiState.preferences.playbackPitch}x", style = MaterialTheme.typography.labelLarge)
                                Slider(
                                    value = uiState.preferences.playbackPitch,
                                    onValueChange = { value -> 
                                        viewModel.updatePreferences { it.copy(playbackPitch = value) }
                                    },
                                    valueRange = 0.5f..2.0f,
                                    steps = 14
                                )
                            }
                        }
                    }

                    item {
                        ListSectionTitle(text = "Gestures", contentPadding = PaddingValues(start = 8.dp, top = 16.dp, bottom = 8.dp))
                    }

                    item {
                        PreferenceSwitch(
                            title = "Mini-Player Swipe",
                            description = "Swipe left/right on mini-player to change tracks",
                            icon = Icons.Rounded.Swipe,
                            isChecked = uiState.preferences.miniPlayerSwipeToSkip,
                            onClick = { 
                                viewModel.updatePreferences { it.copy(miniPlayerSwipeToSkip = !it.miniPlayerSwipeToSkip) }
                            },
                            isFirstItem = true,
                            isLastItem = true
                        )
                    }

                    item {
                        ListSectionTitle(text = "Enhancements", contentPadding = PaddingValues(start = 8.dp, top = 16.dp, bottom = 8.dp))
                    }

                    item {
                        PreferenceSwitch(
                            title = "Skip Silence",
                            description = "Automatically skip silent parts in audio",
                            icon = Icons.AutoMirrored.Rounded.VolumeOff,
                            isChecked = uiState.preferences.skipSilence,
                            onClick = { viewModel.updatePreferences { it.copy(skipSilence = !it.skipSilence) } },
                            isFirstItem = true
                        )
                    }

                    item {
                        PreferenceSwitch(
                            title = "Audio Normalization",
                            description = "Keep volume levels consistent across tracks",
                            icon = Icons.Rounded.Equalizer,
                            isChecked = uiState.preferences.audioNormalization,
                            onClick = { viewModel.updatePreferences { it.copy(audioNormalization = !it.audioNormalization) } }
                        )
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val duration = uiState.preferences.crossfadeDurationMs / 1000f
                                Text("Crossfade Duration: ${"%.1f".format(duration)}s", style = MaterialTheme.typography.labelLarge)
                                Slider(
                                    value = uiState.preferences.crossfadeDurationMs.toFloat(),
                                    onValueChange = { value -> 
                                        viewModel.updatePreferences { it.copy(crossfadeDurationMs = value.toInt()) }
                                    },
                                    valueRange = 0f..5000f,
                                    steps = 10
                                )
                            }
                        }
                    }

                    item {
                        ListSectionTitle(text = "Player Visuals", contentPadding = PaddingValues(start = 8.dp, top = 16.dp, bottom = 8.dp))
                    }

                    item {
                        PreferenceSwitch(
                            title = "Squiggly Progress Bar",
                            description = "Use a wavy progress bar in the player",
                            icon = Icons.Rounded.Gesture,
                            isChecked = uiState.preferences.useSquigglyProgress,
                            onClick = { 
                                viewModel.updatePreferences { it.copy(useSquigglyProgress = !it.useSquigglyProgress) }
                            },
                            isFirstItem = true
                        )
                    }

                    item {
                        PreferenceSwitch(
                            title = "Audio Quality Info",
                            description = "Show sample rate and bitrate in player",
                            icon = Icons.Rounded.HighQuality,
                            isChecked = uiState.preferences.showAudioQuality,
                            onClick = { 
                                viewModel.updatePreferences { it.copy(showAudioQuality = !it.showAudioQuality) }
                            }
                        )
                    }

                    item {
                        PreferenceSlider(
                            title = "Album Art Corners: ${uiState.preferences.albumArtCornerRadius}dp",
                            value = uiState.preferences.albumArtCornerRadius.toFloat(),
                            onValueChange = { value -> 
                                viewModel.updatePreferences { it.copy(albumArtCornerRadius = value.toInt()) }
                            },
                            valueRange = 0f..48f,
                            steps = 12,
                            icon = Icons.Rounded.RoundedCorner,
                            isLastItem = true
                        )
                    }

                    item {
                        ListSectionTitle(text = "Behavior", contentPadding = PaddingValues(start = 8.dp, top = 16.dp, bottom = 8.dp))
                    }

                    item {
                        PreferenceSwitch(
                            title = "Autoplay on Launch",
                            description = "Automatically start playing when app opens",
                            icon = Icons.Rounded.PlayArrow,
                            isChecked = uiState.preferences.autoplayOnLaunch,
                            onClick = { 
                                viewModel.updatePreferences { it.copy(autoplayOnLaunch = !it.autoplayOnLaunch) }
                            },
                            isFirstItem = true
                        )
                    }

                    item {
                        PreferenceSwitch(
                            title = "Stop on Dismiss",
                            description = "Stop playback when app is swiped away from recents",
                            icon = Icons.Rounded.Stop,
                            isChecked = uiState.preferences.stopOnDismiss,
                            onClick = { 
                                viewModel.updatePreferences { it.copy(stopOnDismiss = !it.stopOnDismiss) }
                            },
                            isLastItem = true
                        )
                    }

                    item {
                        ListSectionTitle(text = "Now Playing", contentPadding = PaddingValues(start = 8.dp, top = 16.dp, bottom = 8.dp))
                    }

                    item {
                        PreferenceSlider(
                            title = "Artwork Blur Intensity",
                            value = uiState.preferences.artworkBlurIntensity,
                            onValueChange = { value -> 
                                viewModel.updatePreferences { it.copy(artworkBlurIntensity = value) }
                            },
                            valueRange = 0f..1f,
                            icon = Icons.Rounded.BlurOn,
                            isFirstItem = true,
                            isLastItem = true
                        )
                    }

                    item {
                        ListSectionTitle(text = "Library Filter", contentPadding = PaddingValues(start = 8.dp, top = 16.dp, bottom = 8.dp))
                    }

                    item {
                        PreferenceSwitch(
                            title = "Filter Short Tracks",
                            description = "Hide tracks shorter than ${uiState.preferences.minTrackDurationSeconds}s",
                            icon = Icons.Rounded.FilterList,
                            isChecked = uiState.preferences.filterShortTracks,
                            onClick = { 
                                viewModel.updatePreferences { it.copy(filterShortTracks = !it.filterShortTracks) }
                            },
                            isFirstItem = true
                        )
                    }

                    item {
                        ClickablePreferenceItem(
                            title = "Blacklisted Folders",
                            description = "Manage folders ignored by the library",
                            icon = Icons.Rounded.FolderOff,
                            onClick = { onNavigateToBlacklist() },
                            isLastItem = true
                        )
                    }

                    if (uiState.preferences.filterShortTracks) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Minimum Track Duration: ${uiState.preferences.minTrackDurationSeconds}s", style = MaterialTheme.typography.labelLarge)
                                    Slider(
                                        value = uiState.preferences.minTrackDurationSeconds.toFloat(),
                                        onValueChange = { value -> 
                                            viewModel.updatePreferences { it.copy(minTrackDurationSeconds = value.toInt()) }
                                        },
                                        valueRange = 5f..120f,
                                        steps = 23
                                    )
                                }
                            }
                        }
                    }

                    item {
                        ListSectionTitle(text = "Timer", contentPadding = PaddingValues(start = 8.dp, top = 16.dp, bottom = 8.dp))
                    }

                    item {
                        var showSleepTimerDialog by remember { mutableStateOf(false) }
                        
                        ClickablePreferenceItem(
                            onClick = { showSleepTimerDialog = true },
                            icon = Icons.Rounded.Timer,
                            title = "Sleep Timer",
                            description = if (uiState.preferences.sleepTimerDurationMinutes > 0) "${uiState.preferences.sleepTimerDurationMinutes} minutes" else "Disabled",
                            isFirstItem = true,
                            isLastItem = true
                        )

                        if (showSleepTimerDialog) {
                            TuneoraBottomSheet(
                                onDismissRequest = { showSleepTimerDialog = false },
                                title = "Sleep Timer",
                                dismissButton = {
                                    OutlinedButton(
                                        onClick = { showSleepTimerDialog = false },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            ) {
                                val durations = listOf(0, 15, 30, 45, 60, 90, 120)
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    durations.forEachIndexed { index, minutes ->
                                        TuneoraSegmentedListItem(
                                            selected = uiState.preferences.sleepTimerDurationMinutes == minutes,
                                            onClick = {
                                                viewModel.updatePreferences { it.copy(sleepTimerDurationMinutes = minutes) }
                                                showSleepTimerDialog = false
                                            },
                                            content = { Text(if (minutes == 0) "Disabled" else "$minutes minutes") },
                                            isFirstItem = index == 0,
                                            isLastItem = index == durations.lastIndex
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
    }
}
