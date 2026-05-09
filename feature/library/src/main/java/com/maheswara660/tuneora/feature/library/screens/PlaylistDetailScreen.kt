package com.maheswara660.tuneora.feature.library.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.feature.library.PlaylistDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playlistName: String,
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onGoToAlbum: (String) -> Unit = {},
    onGoToArtist: (String) -> Unit = {},
    onPlayNext: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDelete: (Song) -> Unit = {},
    onNavigateUp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }
    
    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) songs else songs.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
        }
    }
    
    val firstSongArtwork = songs.firstOrNull()?.artworkUri

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
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            } else {
                TuneoraTopAppBar(
                    title = { Text(playlistName) },
                    
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
        if (filteredSongs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TuneoraEmptyState(
                    icon = Icons.AutoMirrored.Rounded.PlaylistPlay,
                    title = if (searchQuery.isEmpty()) "No songs found" else "No matches found",
                    description = if (searchQuery.isEmpty()) "Add some songs to this playlist to see them here." else "Try a different search term"
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
                    // Header (Only show when not searching)
                    if (searchQuery.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            PlaylistHeader(playlistName = playlistName, songCount = songs.size, artworkUri = firstSongArtwork, onPlay = { if (songs.isNotEmpty()) onSongClick(songs.first(), songs) }, onShuffle = { if (songs.isNotEmpty()) onSongClick(songs.shuffled().first(), songs) })
                        }
                    }

                    // Song List
                    items(filteredSongs) { song ->
                        SongGridItem(
                            song = song,
                            onClick = { onSongClick(song, filteredSongs) },
                            onMenuClick = { selectedSongForMenu = song }
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
                    // Header (Only show when not searching)
                    if (searchQuery.isEmpty()) {
                        item {
                            PlaylistHeader(playlistName = playlistName, songCount = songs.size, artworkUri = firstSongArtwork, onPlay = { if (songs.isNotEmpty()) onSongClick(songs.first(), songs) }, onShuffle = { if (songs.isNotEmpty()) onSongClick(songs.shuffled().first(), songs) })
                        }
                    }

                    // Song List
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
    }

    if (selectedSongForMenu != null) {
        val playlists by viewModel.getPlaylists().collectAsStateWithLifecycle(emptyList())
        val context = LocalContext.current
        SongMenuBottomSheet(
            song = selectedSongForMenu!!,
            playlists = playlists,
            onDismissRequest = { selectedSongForMenu = null },
            onPlayNext = { onPlayNext(selectedSongForMenu!!) },
            onAddToQueue = { onAddToQueue(selectedSongForMenu!!) },
            onDelete = { onDelete(selectedSongForMenu!!) },
            onAddToPlaylist = { s, id -> viewModel.addSongToPlaylist(s, id) },
            onCreatePlaylistAndAdd = { s, name -> viewModel.createPlaylistAndAddSong(s, name) },
            onGoToAlbum = onGoToAlbum,
            onGoToArtist = onGoToArtist,
        )
    }

    if (showQuickSettings) {
        QuickSettingsBottomSheet(
            preferences = preferences,
            onDismissRequest = { showQuickSettings = false },
            onUpdatePreferences = { viewModel.updatePreferences(it) },
            onRefreshLibrary = { viewModel.rescanLibrary() },
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToAppearance = onNavigateToAppearance
        )
    }
}

@Composable
private fun PlaylistHeader(
    playlistName: String,
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
                .height(300.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Background Blur/Gradient if possible, or just the image
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp)
            ) {
                AsyncImage(
                    model = artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.3f),
                    error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery)
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
                    modifier = Modifier
                        .size(180.dp),
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp
                ) {
                    AsyncImage(
                        model = artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery)
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
                text = playlistName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "$songCount Songs",
                style = MaterialTheme.typography.titleMedium,
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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Play", style = MaterialTheme.typography.titleMedium)
                }

                FilledTonalButton(
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Shuffle", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
