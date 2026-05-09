package com.maheswara660.tuneora.feature.library.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import com.maheswara660.tuneora.core.common.model.Playlist
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons
import com.maheswara660.tuneora.feature.library.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    onPlaylistClick: (com.maheswara660.tuneora.core.common.model.Playlist) -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onPlayPlaylist: (List<com.maheswara660.tuneora.core.common.model.Song>) -> Unit = {},
    onShufflePlaylist: (List<com.maheswara660.tuneora.core.common.model.Song>) -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var selectedPlaylistForMenu by remember { mutableStateOf<com.maheswara660.tuneora.core.common.model.Playlist?>(null) }
    var playlistToRename by remember { mutableStateOf<com.maheswara660.tuneora.core.common.model.Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<com.maheswara660.tuneora.core.common.model.Playlist?>(null) }

    val filteredPlaylists = remember(playlists, searchQuery) {
        if (searchQuery.isBlank()) playlists else playlists.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    TuneoraScreen(
        topBar = {
            if (isSearchActive) {
                TuneoraTopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search playlists") },
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
                    title = { Text("Playlists") },
                    
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create Playlist")
            }
        }
    ) { padding ->
        if (filteredPlaylists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TuneoraEmptyState(
                    icon = androidx.compose.material.icons.Icons.AutoMirrored.Rounded.PlaylistPlay,
                    title = if (searchQuery.isEmpty()) "No playlists" else "No matches found",
                    description = if (searchQuery.isEmpty()) "Create your first playlist to get started" else "Try a different search term"
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
                    items(filteredPlaylists) { playlist ->
                        PlaylistGridItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            onMenuClick = { selectedPlaylistForMenu = playlist }
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
                    itemsIndexed(filteredPlaylists) { index, playlist ->
                        TuneoraSegmentedListItem(
                            onClick = { onPlaylistClick(playlist) },
                            isFirstItem = index == 0,
                            isLastItem = index == filteredPlaylists.lastIndex,
                            leadingContent = { 
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(androidx.compose.material.icons.Icons.AutoMirrored.Rounded.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            content = { Text(playlist.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                            trailingContent = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { selectedPlaylistForMenu = playlist }) {
                                        Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                                    }
                                    Icon(androidx.compose.material.icons.Icons.Rounded.ChevronRight, contentDescription = null) 
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreatePlaylistBottomSheet(
                onDismiss = { showCreateDialog = false },
                onCreate = { name ->
                    viewModel.createPlaylist(name)
                    showCreateDialog = false
                }
            )
        }

        if (selectedPlaylistForMenu != null) {
            val songsForPlaylist: List<Song> by viewModel.getSongsForPlaylist(selectedPlaylistForMenu!!.id).collectAsStateWithLifecycle(initialValue = emptyList())
            
            PlaylistMenuBottomSheet(
                playlist = selectedPlaylistForMenu!!,
                onDismissRequest = { selectedPlaylistForMenu = null },
                onPlay = { 
                    if (songsForPlaylist.isNotEmpty()) {
                        onPlayPlaylist(songsForPlaylist)
                    }
                },
                onShuffle = { 
                    if (songsForPlaylist.isNotEmpty()) {
                        onShufflePlaylist(songsForPlaylist)
                    }
                },
                onRename = { 
                    playlistToRename = selectedPlaylistForMenu
                    selectedPlaylistForMenu = null
                },
                onDelete = { 
                    playlistToDelete = selectedPlaylistForMenu
                    selectedPlaylistForMenu = null
                }
            )
        }

        if (playlistToRename != null) {
            RenamePlaylistBottomSheet(
                initialName = playlistToRename!!.name,
                onDismissRequest = { playlistToRename = null },
                onRename = { newName ->
                    viewModel.renamePlaylist(playlistToRename!!.id, newName)
                    playlistToRename = null
                }
            )
        }

        if (playlistToDelete != null) {
            DeletePlaylistBottomSheet(
                playlistName = playlistToDelete!!.name,
                onDismissRequest = { playlistToDelete = null },
                onDelete = {
                    viewModel.deletePlaylist(playlistToDelete!!)
                    playlistToDelete = null
                }
            )
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
}

@Composable
private fun PlaylistGridItem(
    playlist: Playlist,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More")
            }
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.PlaylistPlay,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}


