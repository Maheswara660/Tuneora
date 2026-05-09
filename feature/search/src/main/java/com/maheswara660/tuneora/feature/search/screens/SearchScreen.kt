package com.maheswara660.tuneora.feature.search.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.data.domain.SearchResults
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons
import com.maheswara660.tuneora.feature.search.SearchUiEvent
import com.maheswara660.tuneora.feature.search.SearchUiState
import com.maheswara660.tuneora.feature.search.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {},
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onPlayNext: (Song) -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onDelete: (Song) -> Unit = {},
    onGoToAlbum: (String) -> Unit = {},
    onGoToArtist: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }

    SearchScreenContent(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onSongClick = onSongClick,
        onMenuClick = { selectedSongForMenu = it },
        onEvent = viewModel::onEvent,
    )

    if (selectedSongForMenu != null) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreenContent(
    uiState: SearchUiState,
    onNavigateUp: () -> Unit = {},
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onMenuClick: (Song) -> Unit = {},
    onEvent: (SearchUiEvent) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { onEvent(SearchUiEvent.OnQueryChange(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                text = "Search songs, artists, albums",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { onEvent(SearchUiEvent.OnQueryChange("")) }) {
                                    Icon(
                                        imageVector = TuneoraIcons.Close,
                                        contentDescription = "Clear",
                                    )
                                }
                            } else if (uiState.isSearching) {
                                TuneoraLoader(size = 24)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                onEvent(SearchUiEvent.OnSearch(uiState.query))
                                keyboardController?.hide()
                            },
                        ),
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = TuneoraIcons.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        }
    ) { padding ->
        if (uiState.query.isBlank()) {
            SuggestionsContent(
                searchHistory = uiState.searchHistory,
                onHistoryItemClick = { onEvent(SearchUiEvent.OnHistoryItemClick(it)) },
                onRemoveHistoryItem = { onEvent(SearchUiEvent.OnRemoveHistoryItem(it)) },
                onClearHistory = { onEvent(SearchUiEvent.OnClearHistory) },
            )
        } else {
            SearchResultsContent(
                searchResults = uiState.searchResults,
                isSearching = uiState.isSearching,
                preferences = uiState.preferences,
                onSongClick = { song -> onSongClick(song, uiState.searchResults.songs) },
                onMenuClick = onMenuClick,
            )
        }
    }
}

@Composable
private fun SuggestionsContent(
    searchHistory: List<String>,
    onHistoryItemClick: (String) -> Unit,
    onRemoveHistoryItem: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
    ) {
        if (searchHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ListSectionTitle(
                        text = "Recent Searches",
                        contentPadding = PaddingValues(top = 12.dp, bottom = 8.dp),
                    )
                    TextButton(onClick = onClearHistory) {
                        Text(text = "Clear")
                    }
                }
            }

            items(
                items = searchHistory,
                key = { "history_$it" },
            ) { query ->
                SearchHistoryItem(
                    query = query,
                    onClick = { onHistoryItemClick(query) },
                    onRemove = { onRemoveHistoryItem(query) },
                )
            }
        }

        if (searchHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = TuneoraIcons.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Search your music library",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHistoryItem(
    query: String,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    TuneoraSegmentedListItem(
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = TuneoraIcons.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = TuneoraIcons.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        content = {
            Text(
                text = query,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
private fun SearchResultsContent(
    searchResults: SearchResults,
    isSearching: Boolean,
    preferences: com.maheswara660.tuneora.core.common.model.ApplicationPreferences,
    onSongClick: (Song) -> Unit,
    onMenuClick: (Song) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isSearching,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                TuneoraLoader()
            }
        }

        AnimatedVisibility(
            visible = !isSearching,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (searchResults.isEmpty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = TuneoraIcons.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "No results found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                if (preferences.useGridLayout) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchResults.songs) { song ->
                            SongGridItem(
                                song = song,
                                onClick = { onSongClick(song) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(searchResults.songs) { index, song ->
                            SongListItem(
                                song = song,
                                onClick = { onSongClick(song) },
                                onMenuClick = { onMenuClick(song) },
                                isFirstItem = index == 0,
                                isLastItem = index == searchResults.songs.lastIndex
                            )
                        }
                    }
                }
            }
        }
    }
}
