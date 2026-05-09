package com.maheswara660.tuneora.feature.library.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.feature.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistListScreen(
    onArtistClick: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }

    val filteredArtists = remember(artists, searchQuery) {
        if (searchQuery.isBlank()) artists else artists.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    TuneoraScreen(
        topBar = {
            if (isSearchActive) {
                TuneoraTopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search artists") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                errorContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            singleLine = true,
                            leadingIcon = {
                                Icon(com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    isSearchActive = false
                                    searchQuery = ""
                                }) {
                                    Icon(com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons.Close, contentDescription = "Close search")
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            } else {
                TuneoraTopAppBar(
                    title = { Text("Artists") },
                    
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showQuickSettings = true }) {
                            Icon(com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons.QuickSettings, contentDescription = "Quick Settings")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (filteredArtists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TuneoraEmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.Person,
                    title = if (searchQuery.isEmpty()) "No artists" else "No matches found",
                    description = if (searchQuery.isEmpty()) "Your artist collection will appear here" else "Try a different search term"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(filteredArtists) { index, artistName ->
                    val artistSongs = remember(songs, artistName) {
                        songs.filter { it.artist == artistName }
                    }
                    val artworkUri = artistSongs.firstOrNull()?.artworkUri
                    val songCount = artistSongs.size

                    TuneoraSegmentedListItem(
                        onClick = { onArtistClick(artistName) },
                        isFirstItem = index == 0,
                        isLastItem = index == filteredArtists.lastIndex,
                        leadingContent = {
                            TuneoraAlbumArt(
                                model = artworkUri,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                            )
                        },
                        content = {
                            Column {
                                Text(
                                    text = artistName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$songCount Songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingContent = { Icon(androidx.compose.material.icons.Icons.Rounded.ChevronRight, contentDescription = null) }
                    )
                }
            }
        }
    }

    if (showQuickSettings) {
        QuickSettingsBottomSheet(
            preferences = preferences,
            onDismissRequest = { showQuickSettings = false },
            onUpdatePreferences = { viewModel.updatePreferences(it) },
            onRefreshLibrary = { viewModel.refreshLibrary() },
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToAppearance = onNavigateToAppearance
        )
    }
}
