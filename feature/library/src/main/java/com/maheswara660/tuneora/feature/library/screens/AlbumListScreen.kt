package com.maheswara660.tuneora.feature.library.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.feature.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumListScreen(
    onAlbumClick: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }

    val filteredAlbums = remember(albums, searchQuery) {
        if (searchQuery.isBlank()) albums else albums.filter { it.contains(searchQuery, ignoreCase = true) }
    }

     TuneoraScreen(
        topBar = {
            if (isSearchActive) {
                TuneoraTopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search albums") },
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
                    title = { Text("Albums") },
                    
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
        if (filteredAlbums.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TuneoraEmptyState(
                    icon = androidx.compose.material.icons.Icons.Rounded.Album,
                    title = if (searchQuery.isEmpty()) "No albums" else "No matches found",
                    description = if (searchQuery.isEmpty()) "Your album collection will appear here" else "Try a different search term"
                )
            }
        } else {
            if (preferences.useGridLayout) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = padding.calculateTopPadding(), end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredAlbums) { albumName ->
                        val albumSongs = remember(songs, albumName) {
                            songs.filter { it.album == albumName }
                        }
                        val artworkUri = albumSongs.firstOrNull()?.artworkUri
                        val artistName = albumSongs.firstOrNull()?.artist ?: "Unknown Artist"

                        AlbumGridItem(
                            albumName = albumName,
                            artistName = artistName,
                            artworkUri = artworkUri,
                            onClick = { onAlbumClick(albumName) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(filteredAlbums) { index, albumName ->
                        val albumSongs = remember(songs, albumName) {
                            songs.filter { it.album == albumName }
                        }
                        val artworkUri = albumSongs.firstOrNull()?.artworkUri
                        val artistName = albumSongs.firstOrNull()?.artist ?: "Unknown Artist"

                        TuneoraSegmentedListItem(
                            onClick = { onAlbumClick(albumName) },
                            isFirstItem = index == 0,
                            isLastItem = index == filteredAlbums.lastIndex,
                            leadingContent = {
                                TuneoraAlbumArt(
                                    model = artworkUri,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            },
                            content = {
                                Column {
                                    Text(
                                        text = albumName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = artistName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            trailingContent = { Icon(androidx.compose.material.icons.Icons.Rounded.ChevronRight, contentDescription = null) }
                        )
                    }
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

@Composable
private fun AlbumGridItem(
    albumName: String,
    artistName: String,
    artworkUri: android.net.Uri?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        TuneoraAlbumArt(
            model = artworkUri,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = albumName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
