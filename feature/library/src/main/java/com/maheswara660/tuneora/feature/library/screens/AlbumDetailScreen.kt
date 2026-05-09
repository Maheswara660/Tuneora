package com.maheswara660.tuneora.feature.library.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.feature.library.LibraryViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumName: String,
    viewModel: LibraryViewModel = hiltViewModel(),
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onPlayNext: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDelete: (Song) -> Unit = {},
    onNavigateUp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {}
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }
    
    val albumSongs = remember(songs, albumName) {
        songs.filter { it.album == albumName }
    }
    
    val filteredSongs = remember(albumSongs, searchQuery) {
        if (searchQuery.isBlank()) albumSongs else albumSongs.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.artist.contains(searchQuery, ignoreCase = true)
        }
    }
    
    val albumArtwork = albumSongs.firstOrNull()?.artworkUri

    TuneoraScreen(
        topBar = {
            if (isSearchActive) {
                TuneoraTopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search songs") },
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
                                Icon(TuneoraIcons.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    isSearchActive = false
                                    searchQuery = ""
                                }) {
                                    Icon(TuneoraIcons.Close, contentDescription = "Close search")
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            } else {
                TuneoraTopAppBar(
                    title = { Text(albumName) },
                    
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(TuneoraIcons.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showQuickSettings = true }) {
                            Icon(TuneoraIcons.QuickSettings, contentDescription = "Quick Settings")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (filteredSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TuneoraEmptyState(
                    icon = Icons.Rounded.Album,
                    title = if (searchQuery.isEmpty()) "No songs found" else "No matches found",
                    description = if (searchQuery.isEmpty()) "Could not find any songs for this album." else "Try a different search term"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Hero Section (Only show when not searching)
                if (searchQuery.isEmpty()) {
                    item {
                        AlbumHeader(
                            albumName = albumName,
                            artistName = albumSongs.firstOrNull()?.artist ?: "Unknown Artist",
                            songCount = albumSongs.size,
                            artworkUri = albumArtwork,
                            onPlay = { onSongClick(albumSongs.first(), albumSongs) },
                            onShuffle = { onSongClick(albumSongs.shuffled().first(), albumSongs) }
                        )
                    }
                }

                itemsIndexed(filteredSongs) { index, song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song, filteredSongs) },
                        onMenuClick = { selectedSongForMenu = song },
                        isFirstItem = index == 0,
                        isLastItem = index == filteredSongs.lastIndex
                    )
                }
            }
        }
    }

    if (selectedSongForMenu != null) {
        val playlists by viewModel.playlists.collectAsStateWithLifecycle()
        val context = LocalContext.current
        SongMenuBottomSheet(
            song = selectedSongForMenu!!,
            playlists = playlists,
            onDismissRequest = { selectedSongForMenu = null },
            onPlayNext = { onPlayNext(selectedSongForMenu!!) },
            onAddToQueue = { onAddToQueue(selectedSongForMenu!!) },
            onDelete = { onDelete(selectedSongForMenu!!) },
            onAddToPlaylist = { s, id -> viewModel.addSongToPlaylist(id, s.id) },
            onCreatePlaylistAndAdd = { s, name -> viewModel.createPlaylistAndAddSong(s, name) },
            onGoToAlbum = { /* Already here */ },
            onGoToArtist = { artist -> /* Navigate to artist */ },
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

@Composable
private fun AlbumHeader(
    albumName: String,
    artistName: String,
    songCount: Int,
    artworkUri: android.net.Uri?,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp)
            ) {
                com.maheswara660.tuneora.core.ui.components.TuneoraAlbumArt(
                    model = artworkUri,
                    modifier = Modifier.fillMaxSize().alpha(0.2f),
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    com.maheswara660.tuneora.core.ui.components.TuneoraAlbumArt(
                        model = artworkUri,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = albumName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = artistName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$songCount Songs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlay,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Play")
                }

                FilledTonalButton(
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Shuffle")
                }
            }
        }
    }
}
