package com.maheswara660.tuneora.feature.player.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.maheswara660.tuneora.feature.player.NowPlayingViewModel
import com.maheswara660.tuneora.core.ui.theme.LocalApplicationPreferences
import com.maheswara660.tuneora.core.ui.components.*
import com.maheswara660.tuneora.core.ui.util.rememberDominantColor
import com.maheswara660.tuneora.core.common.model.SemanticLyrics
import androidx.compose.ui.platform.LocalContext
import com.maheswara660.tuneora.core.common.model.Song

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {},
    onGoToAlbum: (String) -> Unit = {},
    onGoToArtist: (String) -> Unit = {}
) {
    val song by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val position by viewModel.position.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val shuffleMode by viewModel.shuffleMode.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyrics.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    
    val preferences = LocalApplicationPreferences.current
    val cornerRadius = preferences.cornerRadius.dp
    
    var showMenu by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showPlaybackSpeed by remember { mutableStateOf(false) }
    
    val queue by viewModel.queue.collectAsStateWithLifecycle()
    val sleepTimerActive by viewModel.sleepTimerActive.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()

    var offsetY by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    if (song == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0 || offsetY > 0) {
                            offsetY += dragAmount
                        }
                    },
                    onDragEnd = {
                        if (offsetY > 300) {
                            onNavigateUp()
                        } else {
                            offsetY = 0f
                        }
                    }
                )
            }
    ) {
        // Dynamic Background
        val context = LocalContext.current
        val appPrefs = LocalApplicationPreferences.current
        val dominantColor = com.maheswara660.tuneora.core.ui.util.rememberDominantColor(
            context = context,
            imageUri = song?.artworkUri,
            defaultColor = MaterialTheme.colorScheme.surface
        )
        
        val backgroundColor = if (appPrefs.useDynamicColors) dominantColor else MaterialTheme.colorScheme.background

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 0.4f),
                            backgroundColor.copy(alpha = 0.8f),
                            backgroundColor
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(32.dp))
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onGoToAlbum(song!!.album) }
                ) {
                    Text(
                        text = "PLAYING FROM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = song!!.album,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Main Content Area (Art or Lyrics)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = showLyrics,
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                    },
                    label = "LyricsArtTransition",
                    modifier = Modifier.pointerInput(showLyrics) {
                        if (!showLyrics) {
                            var dragOffset = 0f
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (dragOffset > 100f) {
                                        viewModel.skipToPrevious()
                                    } else if (dragOffset < -100f) {
                                        viewModel.skipToNext()
                                    }
                                    dragOffset = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    dragOffset += dragAmount
                                    change.consume()
                                }
                            )
                        }
                    }
                ) { targetShowLyrics ->
                    if (targetShowLyrics) {
                        LyricsView(
                            lyrics = lyrics,
                            currentPosition = position,
                            onLyricsClick = { showLyrics = false }
                        )
                    } else {
                        TuneoraAlbumArt(
                            model = song!!.artworkUri,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .combinedClickable(
                                    onClick = { if (lyrics != null) showLyrics = true },
                                    onLongClick = { onGoToAlbum(song!!.album) }
                                ),
                            shape = RoundedCornerShape(appPrefs.albumArtCornerRadius.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Metadata & Controls Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                val appPrefs = LocalApplicationPreferences.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song!!.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { onGoToAlbum(song!!.album) }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = song!!.artist,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clickable { onGoToArtist(song!!.artist) }
                            )
                            
                            if (appPrefs.showAudioQuality) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "44.1kHz / 16bit", // Mock info, real info should come from PlaybackManager
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    IconButton(onClick = { viewModel.toggleFavorite(song!!) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Bar
                val progress = if (duration > 0) position.toFloat() / duration else 0f
                
                if (appPrefs.useSquigglyProgress) {
                    SquigglySlider(
                        value = progress,
                        onValueChange = { viewModel.seekTo((it * duration).toLong()) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Slider(
                        value = progress,
                        onValueChange = { viewModel.seekTo((it * duration).toLong()) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(position), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(formatTime(duration), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Playback Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (shuffleMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    IconButton(onClick = { viewModel.skipToPrevious() }) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(48.dp))
                    }

                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.togglePlayPause() },
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play/Pause",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.skipToNext() }) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(48.dp))
                    }

                    IconButton(onClick = { viewModel.toggleRepeat() }) {
                        Icon(
                            imageVector = when (repeatMode) {
                                androidx.media3.common.Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Bar Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showSleepTimer = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepTimerActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                IconButton(onClick = { showPlaybackSpeed = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = "Playback Speed",
                        tint = if (playbackSpeed != 1.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                IconButton(onClick = { showLyrics = !showLyrics }) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = "Lyrics",
                        tint = if (showLyrics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                IconButton(onClick = { showQueue = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = if (showQueue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        if (showPlaybackSpeed) {
            PlaybackSpeedBottomSheet(
                currentSpeed = playbackSpeed,
                onSpeedChange = viewModel::updatePlaybackSpeed,
                onDismissRequest = { showPlaybackSpeed = false }
            )
        }

        if (showSleepTimer) {
            SleepTimerBottomSheet(
                onDismissRequest = { showSleepTimer = false },
                onTimerSelected = { duration -> viewModel.setSleepTimer(duration) },
                onCancelTimer = { viewModel.cancelSleepTimer() },
                isTimerActive = sleepTimerActive
            )
        }

        if (showQueue) {
            QueueBottomSheet(
                queue = queue,
                currentSong = song,
                onDismissRequest = { showQueue = false },
                onSongClick = { s -> 
                    viewModel.playQueueItem(s)
                    showQueue = false
                },
                onMove = viewModel::moveQueueItem,
                onRemove = viewModel::removeQueueItem
            )
        }

        if (showMenu && song != null) {
            SongMenuBottomSheet(
                song = song!!,
                playlists = playlists,
                onDismissRequest = { showMenu = false },
                onAddToPlaylist = { s, id -> viewModel.addSongToPlaylist(s, id) },
                onCreatePlaylistAndAdd = { s, name -> viewModel.createPlaylistAndAddSong(s, name) },
                onPlayNext = { viewModel.playNext(song!!) },
                onAddToQueue = { viewModel.addToQueue(song!!) },
                onGoToAlbum = { album ->
                    showMenu = false
                    onGoToAlbum(album)
                },
                onGoToArtist = { artist ->
                    showMenu = false
                    onGoToArtist(artist)
                },
                onDelete = { viewModel.deleteSong(context, song!!) }
            )
        }
    }
}

@Composable
fun LyricsView(
    lyrics: SemanticLyrics?,
    currentPosition: Long,
    onLyricsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onLyricsClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (lyrics == null) {
            Text(
                text = "No lyrics available",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            val scrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start
            ) {
                when (lyrics) {
                    is SemanticLyrics.UnsyncedLyrics -> {
                        lyrics.unsyncedText.forEach { (line, _) ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    is SemanticLyrics.SyncedLyrics -> {
                        lyrics.text.forEach { line ->
                            val isActive = currentPosition >= line.start.toLong() && currentPosition <= line.end.toLong()
                            val color by animateColorAsState(
                                targetValue = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                label = "LyricColor"
                            )
                            val scale by animateFloatAsState(
                                targetValue = if (isActive) 1.05f else 1f,
                                label = "LyricScale"
                            )
                            
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = (MaterialTheme.typography.headlineSmall.fontSize.value * scale).sp
                                ),
                                color = color,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis < 0) return "0:00"
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60
    return "%d:%02d".format(minutes, seconds)
}
