package com.maheswara660.tuneora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.maheswara660.tuneora.core.ui.navigation.NavigationItem
import com.maheswara660.tuneora.core.ui.theme.TuneoraTheme
import com.maheswara660.tuneora.feature.history.screens.HistoryScreen
import com.maheswara660.tuneora.feature.home.screens.HomeScreen
import com.maheswara660.tuneora.feature.library.screens.LibraryScreen
import com.maheswara660.tuneora.feature.library.screens.PlaylistScreen
import com.maheswara660.tuneora.feature.library.screens.PlaylistDetailScreen
import com.maheswara660.tuneora.feature.library.screens.FavoritesScreen
import com.maheswara660.tuneora.feature.library.screens.FolderDetailScreen
import com.maheswara660.tuneora.feature.library.screens.AlbumDetailScreen
import com.maheswara660.tuneora.feature.library.screens.ArtistDetailScreen
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.feature.search.screens.SearchScreen
import com.maheswara660.tuneora.feature.settings.screens.SettingsScreen
import com.maheswara660.tuneora.feature.settings.screens.AppearanceScreen
import com.maheswara660.tuneora.feature.settings.screens.PlaybackSettingsScreen
import com.maheswara660.tuneora.feature.settings.screens.BlacklistScreen
import com.maheswara660.tuneora.feature.settings.screens.about.AboutScreen
import com.maheswara660.tuneora.core.ui.components.MiniPlayer
import com.maheswara660.tuneora.feature.player.screens.NowPlayingScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.maheswara660.tuneora.feature.settings.screens.general.GeneralPreferencesScreen
import com.maheswara660.tuneora.feature.settings.screens.decoder.DecoderPreferencesScreen
import com.maheswara660.tuneora.feature.settings.screens.audio.AudioPreferencesScreen
import com.maheswara660.tuneora.feature.settings.screens.medialibrary.MediaLibraryPreferencesScreen
import com.maheswara660.tuneora.feature.library.screens.AlbumListScreen
import com.maheswara660.tuneora.feature.library.screens.ArtistListScreen
import com.maheswara660.tuneora.feature.library.screens.FolderListScreen
import com.maheswara660.tuneora.feature.library.screens.RecentlyAddedScreen
import com.maheswara660.tuneora.feature.library.LibraryViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val mainViewModel: MainViewModel = hiltViewModel()
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            val currentSong by mainViewModel.currentSong.collectAsStateWithLifecycle()
            val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()

            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            var hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                )
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasPermission = isGranted
                if (isGranted) {
                    // Trigger a re-scan if needed, though LocalMediaSynchronizer might pick it up
                }
            }

            val folderPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree()
            ) { uri ->
                uri?.let {
                    // Take persistable permission
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    // Add folder to repository (via MainViewModel)
                    // mainViewModel.addFolder(it.toString())
                }
            }

            LaunchedEffect(Unit) {
                if (!hasPermission) {
                    permissionLauncher.launch(permission)
                }
                mainViewModel.handleIntent(intent)
            }
            
            DisposableEffect(Unit) {
                onDispose { }
            }
            TuneoraTheme(preferences = uiState) {

                val libraryViewModel: LibraryViewModel = hiltViewModel()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val isTopLevelDestination = NavigationItem.items.any { it.route == currentDestination?.route }
                val isPlayerScreen = currentDestination?.route == "player"
                val currentSong by mainViewModel.currentSong.collectAsStateWithLifecycle()
                val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
                val position by mainViewModel.playbackManager.position.collectAsStateWithLifecycle()
                val duration by mainViewModel.playbackManager.duration.collectAsStateWithLifecycle()
                val progress = if (duration > 0) position.toFloat() / duration else 0f

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,

                    bottomBar = {
                        AnimatedVisibility(
                            visible = !isPlayerScreen,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            Column {
                                if (currentSong != null) {
                                    MiniPlayer(
                                        song = currentSong,
                                        isPlaying = isPlaying,
                                        onTogglePlayPause = { mainViewModel.togglePlayPause() },
                                        onClick = { navController.navigate("player") },
                                        onNext = { mainViewModel.playbackManager.skipToNext() },
                                        onPrevious = { mainViewModel.playbackManager.skipToPrevious() },
                                        onFavoriteClick = { mainViewModel.toggleFavorite(currentSong!!) },
                                        isFavorite = currentSong?.isFavorite ?: false,
                                        progress = progress,
                                        swipeEnabled = uiState.miniPlayerSwipeToSkip,
                                        isFloating = isTopLevelDestination
                                    )
                                }


                                
                                AnimatedVisibility(
                                    visible = isTopLevelDestination,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                        tonalElevation = 0.dp
                                    ) {
                                        NavigationItem.items.forEach { item ->
                                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                            NavigationBarItem(
                                                icon = { Icon(item.icon, contentDescription = item.title) },
                                                label = { Text(item.title) },
                                                selected = selected,
                                                onClick = {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = NavigationItem.Home.route,
                        modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
                    ) {
                        composable(NavigationItem.Home.route) {
                            HomeScreen(
                                onSearchClick = { navController.navigate("search") },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") },
                                onSongClick = { song, list -> mainViewModel.playbackManager.playSong(song, list) },
                                onPlayNext = { song -> mainViewModel.playbackManager.playNext(song) },
                                onAddToQueue = { song -> mainViewModel.playbackManager.addToQueue(song) },
                                onDelete = { song -> libraryViewModel.deleteSong(song) },
                                onGoToAlbum = { album -> navController.navigate("album_detail/${android.net.Uri.encode(album)}") },
                                onGoToArtist = { artist -> navController.navigate("artist_detail/${android.net.Uri.encode(artist)}") }
                            )
                        }
                        composable("player") {
                            NowPlayingScreen(
                                onNavigateUp = { navController.navigateUp() },
                                onGoToAlbum = { album -> 
                                    navController.navigate("album_detail/${android.net.Uri.encode(album)}")
                                },
                                onGoToArtist = { artist -> 
                                    navController.navigate("artist_detail/${android.net.Uri.encode(artist)}")
                                }
                            )
                        }
                        composable(NavigationItem.Library.route) {
                            LibraryScreen(
                                onSongClick = { song, list -> mainViewModel.playbackManager.playSong(song, list) },
                                onPlayNext = { song -> mainViewModel.playbackManager.playNext(song) },
                                onAddToQueue = { song -> mainViewModel.playbackManager.addToQueue(song) },
                                onDelete = { song -> libraryViewModel.deleteSong(song) },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") },
                                onFolderClick = { folder -> navController.navigate("folder_detail/${android.net.Uri.encode(folder.path)}/${folder.name}") },
                                onPlaylistClick = { playlist -> navController.navigate("playlist/${playlist.id}/${playlist.name}") },
                                onPlaylistsClick = { navController.navigate("playlists") },
                                onFavoritesClick = { navController.navigate("favorites") },
                                onAlbumClick = { album -> navController.navigate("album_detail/${android.net.Uri.encode(album)}") },
                                onArtistClick = { artist -> navController.navigate("artist_detail/${android.net.Uri.encode(artist)}") },
                                onRecentlyAddedClick = { navController.navigate("recently_added") },
                                onAlbumsClick = { navController.navigate("albums") },
                                onArtistsClick = { navController.navigate("artists") },
                                onFoldersClick = { navController.navigate("folders") }
                            )
                        }
                        composable(
                            "folder_detail/{folderPath}/{folderName}",
                            arguments = listOf(
                                navArgument("folderPath") { type = NavType.StringType },
                                navArgument("folderName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val folderPath = android.net.Uri.decode(backStackEntry.arguments?.getString("folderPath") ?: "")
                            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
                            FolderDetailScreen(
                                folderPath = folderPath,
                                folderName = folderName,
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") },
                                onSongClick = { song, list -> mainViewModel.playbackManager.playSong(song, list) },
                                onPlayNext = { song -> mainViewModel.playbackManager.playNext(song) },
                                onAddToQueue = { song -> mainViewModel.playbackManager.addToQueue(song) },
                                onDelete = { song -> libraryViewModel.deleteSong(song) },
                                onGoToAlbum = { album -> navController.navigate("album_detail/${android.net.Uri.encode(album)}") },
                                onGoToArtist = { artist -> navController.navigate("artist_detail/${android.net.Uri.encode(artist)}") }
                            )
                        }
                        composable("favorites") {
                            FavoritesScreen(
                                onSongClick = { song, list -> mainViewModel.playbackManager.playSong(song, list) },
                                onPlayNext = { song -> mainViewModel.playbackManager.playNext(song) },
                                onAddToQueue = { song -> mainViewModel.playbackManager.addToQueue(song) },
                                onDelete = { song -> libraryViewModel.deleteSong(song) },
                                onGoToAlbum = { album -> navController.navigate("album_detail/${android.net.Uri.encode(album)}") },
                                onGoToArtist = { artist -> navController.navigate("artist_detail/${android.net.Uri.encode(artist)}") },
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") }
                            )
                        }

                        composable("playlists") {
                            PlaylistScreen(
                                onPlaylistClick = { playlist -> navController.navigate("playlist/${playlist.id}/${playlist.name}") },
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") },
                                onPlayPlaylist = { list -> mainViewModel.playbackManager.playSong(list.first(), list) },
                                onShufflePlaylist = { list -> 
                                    val shuffled = list.shuffled()
                                    mainViewModel.playbackManager.playSong(shuffled.first(), shuffled)
                                }
                            )
                        }

                        composable(
                            "playlist/{playlistId}/{playlistName}",
                            arguments = listOf(
                                navArgument("playlistId") { type = NavType.LongType },
                                navArgument("playlistName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                            val playlistName = backStackEntry.arguments?.getString("playlistName") ?: ""
                            PlaylistDetailScreen(
                                playlistId = playlistId,
                                playlistName = playlistName,
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") },
                                onSongClick = { song, list -> mainViewModel.playbackManager.playSong(song, list) },
                                onPlayNext = { song -> mainViewModel.playbackManager.playNext(song) },
                                onAddToQueue = { song -> mainViewModel.playbackManager.addToQueue(song) },
                                onDelete = { song -> libraryViewModel.deleteSong(song) },
                                onGoToAlbum = { album -> navController.navigate("album_detail/${android.net.Uri.encode(album)}") },
                                onGoToArtist = { artist -> navController.navigate("artist_detail/${android.net.Uri.encode(artist)}") }
                            )
                        }

                        composable(
                            "album_detail/{albumName}",
                            arguments = listOf(navArgument("albumName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val albumName = backStackEntry.arguments?.getString("albumName") ?: ""
                            AlbumDetailScreen(
                                albumName = albumName,
                                onSongClick = { song: Song, list: List<Song> -> mainViewModel.playbackManager.playSong(song, list) },
                                onPlayNext = { song: Song -> mainViewModel.playbackManager.playNext(song) },
                                onAddToQueue = { song: Song -> mainViewModel.playbackManager.addToQueue(song) },
                                onDelete = { song: Song -> libraryViewModel.deleteSong(song) },
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") }
                            )
                        }

                        composable(
                            "artist_detail/{artistName}",
                            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
                            ArtistDetailScreen(
                                artistName = artistName,
                                onSongClick = { song: Song, list: List<Song> -> mainViewModel.playbackManager.playSong(song, list) },
                                onPlayNext = { song: Song -> mainViewModel.playbackManager.playNext(song) },
                                onAddToQueue = { song: Song -> mainViewModel.playbackManager.addToQueue(song) },
                                onDelete = { song: Song -> libraryViewModel.deleteSong(song) },
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") }
                            )
                        }
                        
                        composable("albums") {
                            AlbumListScreen(
                                onAlbumClick = { album -> navController.navigate("album_detail/${android.net.Uri.encode(album)}") },
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") }
                            )
                        }
                        
                        composable("artists") {
                            ArtistListScreen(
                                onArtistClick = { artist -> navController.navigate("artist_detail/${android.net.Uri.encode(artist)}") },
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") }
                            )
                        }
                        
                        composable("folders") {
                            FolderListScreen(
                                onFolderClick = { folder -> navController.navigate("folder_detail/${android.net.Uri.encode(folder.path)}/${folder.name}") },
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") }
                            )
                        }

                        composable("recently_added") {
                            RecentlyAddedScreen(
                                onSongClick = { song: Song, list: List<Song> -> mainViewModel.playbackManager.playSong(song, list) },
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") }
                            )
                        }

                        composable("search") {
                            SearchScreen(
                                onNavigateUp = { navController.navigateUp() },
                                onSongClick = { song, list -> mainViewModel.playbackManager.playSong(song, list) },
                                onPlayNext = { song -> mainViewModel.playbackManager.playNext(song) },
                                onAddToQueue = { song -> mainViewModel.playbackManager.addToQueue(song) },
                                onDelete = { song -> libraryViewModel.deleteSong(song) },
                                onGoToAlbum = { album -> navController.navigate("album_detail/${android.net.Uri.encode(album)}") },
                                onGoToArtist = { artist -> navController.navigate("artist_detail/${android.net.Uri.encode(artist)}") }
                            )
                        }
                        composable(NavigationItem.History.route) {
                            HistoryScreen(
                                onSongClick = { song, list -> mainViewModel.playbackManager.playSong(song, list) },
                                onPlayNext = { song -> mainViewModel.playbackManager.playNext(song) },
                                onAddToQueue = { song -> mainViewModel.playbackManager.addToQueue(song) },
                                onDelete = { song -> libraryViewModel.deleteSong(song) },
                                onGoToAlbum = { album -> navController.navigate("album_detail/${android.net.Uri.encode(album)}") },
                                onGoToArtist = { artist -> navController.navigate("artist_detail/${android.net.Uri.encode(artist)}") },
                                onNavigateToSettings = { navController.navigate(NavigationItem.Settings.route) },
                                onNavigateToAppearance = { navController.navigate("appearance") }
                            )
                        }
                        composable(NavigationItem.Settings.route) {
                            SettingsScreen(
                                onAppearanceClick = { navController.navigate("appearance") },
                                onPlaybackClick = { navController.navigate("playback_settings") },
                                onMediaLibraryClick = { navController.navigate("media_library") },
                                onDecoderClick = { navController.navigate("decoder") },
                                onAudioClick = { navController.navigate("audio") },
                                onGeneralClick = { navController.navigate("general") },
                                onAboutClick = { navController.navigate("about") }
                            )
                        }
                        composable("media_library") {
                            MediaLibraryPreferencesScreen(
                                onNavigateUp = { navController.navigateUp() },
                                onManageFoldersClick = { folderPickerLauncher.launch(null) }
                            )
                        }
                        composable("decoder") {
                            DecoderPreferencesScreen(
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("audio") {
                            AudioPreferencesScreen(
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("general") {
                            GeneralPreferencesScreen(
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("playback_settings") {
                            PlaybackSettingsScreen(
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToBlacklist = { navController.navigate(NavigationItem.Blacklist.route) }
                            )
                        }

                        composable(NavigationItem.Blacklist.route) {
                            BlacklistScreen(
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }

                        composable("appearance") {
                            AppearanceScreen(
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("about") {
                            val context = android.view.View(LocalContext.current).context // To get activity context if needed, or just use LocalContext
                            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                            AboutScreen(
                                onLibrariesClick = { 
                                    uriHandler.openUri("https://github.com/maheswara660/Tuneora/blob/main/README.md#acknowledgments")
                                },
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}