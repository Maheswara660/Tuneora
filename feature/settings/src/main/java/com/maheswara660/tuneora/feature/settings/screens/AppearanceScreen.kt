package com.maheswara660.tuneora.feature.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.common.model.ThemeConfig
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.core.ui.theme.CustomAccents
import com.maheswara660.tuneora.feature.settings.SettingsUiEvent
import com.maheswara660.tuneora.feature.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeSheet by remember { mutableStateOf(false) }
    var showAccentSheet by remember { mutableStateOf(false) }

    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = { Text("Appearance") },
                
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
                ListSectionTitle(text = "Theming", contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 8.dp))
            }
            item {
                ClickablePreferenceItem(
                    onClick = { showThemeSheet = true },
                    icon = Icons.Rounded.Palette,
                    title = "Theme Mode",
                    description = uiState.preferences.themeConfig.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                    isFirstItem = true
                )
            }
            item {
                PreferenceSwitch(
                    title = "Pure Dark Mode",
                    description = "Use absolute black background for OLED screens",
                    icon = Icons.Rounded.Contrast,
                    isChecked = uiState.preferences.useHighContrastDarkTheme,
                    onClick = { 
                        viewModel.updatePreferences { it.copy(useHighContrastDarkTheme = !it.useHighContrastDarkTheme) }
                    }
                )
            }
            item {
                PreferenceSwitch(
                    title = "Dynamic Colors",
                    description = "Extract colors from album art for the player UI",
                    icon = Icons.Rounded.ColorLens,
                    isChecked = uiState.preferences.useDynamicColors,
                    onClick = { 
                        viewModel.updatePreferences { it.copy(useDynamicColors = !it.useDynamicColors) }
                    }
                )
            }
            item {
                ClickablePreferenceItem(
                    onClick = { showAccentSheet = true },
                    icon = Icons.Rounded.Brush,
                    title = "Accent Color",
                    description = if (uiState.preferences.accentColorIndex == -1) "Material You (Dynamic)" else CustomAccents.getOrNull(uiState.preferences.accentColorIndex)?.name ?: "Custom",
                    isLastItem = true
                )
            }

            item {
                ListSectionTitle(text = "Layout", contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 8.dp))
            }
            item {
                PreferenceSwitch(
                    title = "Grid Layout",
                    description = "Show library items in a grid instead of a list",
                    icon = Icons.Rounded.GridView,
                    isChecked = uiState.preferences.useGridLayout,
                    onClick = { 
                        viewModel.updatePreferences { it.copy(useGridLayout = !it.useGridLayout) }
                    },
                    isFirstItem = true,
                    isLastItem = true
                )
            }

            item {
                ListSectionTitle(text = "Player Visuals", contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 8.dp))
            }
            item {
                PreferenceSwitch(
                    title = "Glowing Lyrics",
                    description = "Add a subtle glow effect to active lyrics",
                    icon = Icons.Rounded.AutoAwesome,
                    isChecked = uiState.preferences.enableGlowLyrics,
                    onClick = { 
                        viewModel.updatePreferences { it.copy(enableGlowLyrics = !it.enableGlowLyrics) }
                    },
                    isFirstItem = true
                )
            }
            item {
                PreferenceSwitch(
                    title = "High Quality Artwork",
                    description = "Display higher resolution album art",
                    icon = Icons.Rounded.Image,
                    isChecked = uiState.preferences.artworkQuality == "High",
                    onClick = { 
                        viewModel.updatePreferences { it.copy(artworkQuality = if (it.artworkQuality == "High") "Low" else "High") }
                    },
                    isLastItem = true
                )
            }

            item {
                ListSectionTitle(text = "System Polish", contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 8.dp))
            }
            item {
                PreferenceSlider(
                    title = "Corner Radius: ${uiState.preferences.cornerRadius}dp",
                    value = uiState.preferences.cornerRadius.toFloat(),
                    onValueChange = { value -> 
                        viewModel.updatePreferences { it.copy(cornerRadius = value.toInt()) }
                    },
                    valueRange = 0f..32f,
                    steps = 31,
                    icon = Icons.Rounded.RoundedCorner,
                    isFirstItem = true
                )
            }
            item {
                PreferenceSlider(
                    title = "Animation Speed: ${uiState.preferences.animationSpeedMultiplier}x",
                    value = uiState.preferences.animationSpeedMultiplier,
                    onValueChange = { value -> 
                        viewModel.updatePreferences { it.copy(animationSpeedMultiplier = value) }
                    },
                    valueRange = 0.5f..2.0f,
                    steps = 2,
                    icon = Icons.Rounded.Speed,
                    isLastItem = true
                )
            }
        }
    }

    if (showThemeSheet) {
        ThemeBottomSheet(
            currentTheme = uiState.preferences.themeConfig,
            onDismiss = { showThemeSheet = false },
            onThemeSelected = {
                viewModel.onEvent(SettingsUiEvent.UpdateThemeConfig(it))
                showThemeSheet = false
            }
        )
    }

    if (showAccentSheet) {
        AccentBottomSheet(
            currentIndex = uiState.preferences.accentColorIndex,
            onDismiss = { showAccentSheet = false },
            onAccentSelected = {
                viewModel.onEvent(SettingsUiEvent.UpdateAccentColorIndex(it))
                showAccentSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeBottomSheet(
    currentTheme: ThemeConfig,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeConfig) -> Unit
) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    
    TuneoraBottomSheet(
        onDismissRequest = onDismiss,
        title = "Select Theme",
        confirmButton = {
            Button(
                onClick = { 
                    onThemeSelected(selectedTheme)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val themes = ThemeConfig.entries
            themes.forEachIndexed { index, theme ->
                val themeName = theme.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
                val isFirst = index == 0
                val isLast = index == themes.lastIndex
                TuneoraSegmentedListItem(
                    onClick = { selectedTheme = theme },
                    leadingContent = {
                        RadioButton(
                            selected = selectedTheme == theme,
                            onClick = null
                        )
                    },
                    content = { Text(themeName) },
                    isFirstItem = isFirst,
                    isLastItem = isLast
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccentBottomSheet(
    currentIndex: Int,
    onDismiss: () -> Unit,
    onAccentSelected: (Int) -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(currentIndex) }
    
    TuneoraBottomSheet(
        onDismissRequest = onDismiss,
        title = "Select Accent",
        confirmButton = {
            Button(
                onClick = { 
                    onAccentSelected(selectedIndex)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                TuneoraSegmentedListItem(
                    onClick = { selectedIndex = -1 },
                    leadingContent = {
                        RadioButton(
                            selected = selectedIndex == -1,
                            onClick = null
                        )
                    },
                    content = { Text("Material You (Dynamic)") },
                    isFirstItem = true,
                    isLastItem = CustomAccents.isEmpty()
                )
            }
            
            itemsIndexed(CustomAccents) { index, accent ->
                TuneoraSegmentedListItem(
                    onClick = { selectedIndex = index },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(accent.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedIndex == index) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    content = { Text(accent.name) },
                    isFirstItem = false,
                    isLastItem = index == CustomAccents.lastIndex
                )
            }
        }
    }
}
