package com.maheswara660.tuneora.feature.library.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maheswara660.tuneora.core.common.model.Folder
import com.maheswara660.tuneora.core.common.model.Playlist
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons
import com.maheswara660.tuneora.feature.library.LibraryViewModel
import com.maheswara660.tuneora.core.ui.theme.LocalApplicationPreferences
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onFolderClick: (Folder) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onPlaylistsClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onAlbumClick: (String) -> Unit = {},
    onArtistClick: (String) -> Unit = {},
    onPlayNext: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDelete: (Song) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onRecentlyAddedClick: () -> Unit = {},
    onAlbumsClick: () -> Unit = {},
    onArtistsClick: () -> Unit = {},
    onFoldersClick: () -> Unit = {}
) {
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val filteredRecentlyAdded = remember(recentlyAdded, searchQuery) {
        if (searchQuery.isBlank()) recentlyAdded else recentlyAdded.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }
    }

    val appPrefs = LocalApplicationPreferences.current
    val cornerRadius = appPrefs.cornerRadius.dp

    var showQuickSettings by remember { mutableStateOf(false) }
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }

    TuneoraScreen(
        topBar = {
            if (isSearchActive) {
                TuneoraTopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search library") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
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
                    }
                )
            } else {
                TuneoraTopAppBar(
                    title = { Text("Library") },
                    
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
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                LibraryNavigationItem(
                    title = "Favorites",
                    description = "Your liked songs and artists",
                    icon = Icons.Rounded.Favorite,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    isFirstItem = true,
                    onClick = onFavoritesClick
                )
            }
            item {
                LibraryNavigationItem(
                    title = "Recently Added",
                    description = "Songs added in the last 30 days",
                    icon = Icons.Rounded.Schedule,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onRecentlyAddedClick
                )
            }
            item {
                LibraryNavigationItem(
                    title = "Albums",
                    description = "Browse your music by album",
                    icon = Icons.Rounded.Album,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = onAlbumsClick
                )
            }
            item {
                LibraryNavigationItem(
                    title = "Artists",
                    description = "Browse your music by artist",
                    icon = Icons.Rounded.Person,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onArtistsClick
                )
            }
            item {
                LibraryNavigationItem(
                    title = "Playlists",
                    description = "Your personal music collections",
                    icon = Icons.AutoMirrored.Rounded.PlaylistPlay,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onPlaylistsClick
                )
            }
            item {
                LibraryNavigationItem(
                    title = "Folders",
                    description = "Browse music by storage location",
                    icon = Icons.Rounded.Folder,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    isLastItem = true,
                    onClick = onFoldersClick
                )
            }

        }
    }

    if (selectedSongForMenu != null) {
        val playlistsByViewModel by viewModel.playlists.collectAsStateWithLifecycle()
        val context = LocalContext.current
        SongMenuBottomSheet(
            song = selectedSongForMenu!!,
            playlists = playlistsByViewModel,
            onDismissRequest = { selectedSongForMenu = null },
            onPlayNext = { onPlayNext(selectedSongForMenu!!) },
            onAddToQueue = { onAddToQueue(selectedSongForMenu!!) },
            onDelete = { onDelete(selectedSongForMenu!!) },
            onAddToPlaylist = { song, playlistId -> viewModel.addSongToPlaylist(playlistId, song.id) },
            onCreatePlaylistAndAdd = { song, name -> viewModel.createPlaylistAndAddSong(song, name) },
            onGoToAlbum = onAlbumClick,
            onGoToArtist = onArtistClick,
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
fun LibraryNavigationItem(
    title: String,
    description: String? = null,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false
) {
    TuneoraSegmentedListItem(
        onClick = onClick,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            }
        },
        content = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = description?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    )
}
