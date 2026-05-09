package com.maheswara660.tuneora.feature.home.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons
import com.maheswara660.tuneora.core.ui.state.rememberSelectionManager
import com.maheswara660.tuneora.feature.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSearchClick: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onPlayNext: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDelete: (Song) -> Unit = {},
    onGoToAlbum: (String) -> Unit = {},
    onGoToArtist: (String) -> Unit = {}
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    
    val selectionManager = rememberSelectionManager()
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var showMultiDeleteDialog by remember { mutableStateOf(false) }

    TuneoraScreen(
        topBar = {
            if (selectionManager.isInSelectionMode) {
                TuneoraTopAppBar(
                    title = { Text("${selectionManager.selectedCount} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectionManager.exitSelectionMode() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Exit selection")
                        }
                    },
                    actions = {
                        val context = LocalContext.current
                        IconButton(onClick = { 
                            val selectedSongs = selectionManager.selectedSongs.toList()
                            if (selectedSongs.isNotEmpty()) {
                                val uris = ArrayList(selectedSongs.map { android.net.Uri.parse(it.uriString) })
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
                                    type = "audio/*"
                                    putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, uris)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Songs"))
                            }
                        }) {
                            Icon(Icons.Rounded.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { 
                            if (selectionManager.selectedSongs.isNotEmpty()) {
                                showMultiDeleteDialog = true
                            }
                        }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                        }
                    }
                )
            } else {
                TuneoraTopAppBar(
                    title = { Text("Tuneora") },
                    
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(imageVector = TuneoraIcons.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showQuickSettings = true }) {
                            Icon(imageVector = TuneoraIcons.QuickSettings, contentDescription = "Quick Settings")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (songs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TuneoraEmptyState(
                    icon = TuneoraIcons.MusicNote,
                    title = "No songs found",
                    description = "Scan your device to find music files and start listening."
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
                    items(songs) { song ->
                        val isSelected = selectionManager.isSongSelected(song)
                        SongGridItem(
                            song = song,
                            onClick = {
                                if (selectionManager.isInSelectionMode) {
                                    selectionManager.toggleSongSelection(song)
                                } else {
                                    onSongClick(song, songs)
                                }
                            },
                            onMenuClick = { selectedSongForMenu = song },
                            modifier = Modifier.padding(if (isSelected) 4.dp else 0.dp) // Visual feedback for selection
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
                    itemsIndexed(songs) { index, song ->
                        val isSelected = selectionManager.isSongSelected(song)
                        SongListItem(
                            song = song,
                            isSelected = isSelected,
                            onClick = {
                                if (selectionManager.isInSelectionMode) {
                                    selectionManager.toggleSongSelection(song)
                                } else {
                                    onSongClick(song, songs)
                                }
                            },
                            onLongClick = {
                                selectionManager.toggleSongSelection(song)
                            },
                            onMenuClick = { selectedSongForMenu = song },
                            isFirstItem = index == 0,
                            isLastItem = index == songs.lastIndex
                        )
                    }
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
            onAddToPlaylist = { song, playlistId -> viewModel.addSongToPlaylist(song, playlistId) },
            onCreatePlaylistAndAdd = { song, name -> viewModel.createPlaylistAndAddSong(song, name) },
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

    if (showMultiDeleteDialog) {
        val selectedSongs = remember(selectionManager.selectedSongs, songs) {
            songs.filter { song -> selectionManager.isSongSelected(song) }
        }
        DeleteConfirmationBottomSheet(
            songs = selectedSongs,
            onDismiss = { showMultiDeleteDialog = false },
            onConfirm = {
                viewModel.deleteSongs(selectedSongs)
                selectionManager.exitSelectionMode()
                showMultiDeleteDialog = false
            }
        )
    }
}
