package com.maheswara660.tuneora.core.media

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.maheswara660.tuneora.core.common.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import androidx.media3.common.PlaybackParameters
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.maheswara660.tuneora.core.common.model.SemanticLyrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(

    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val musicRepository: com.maheswara660.tuneora.core.data.repository.MusicRepository,
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _lyrics = MutableStateFlow<SemanticLyrics?>(null)
    val lyrics: StateFlow<SemanticLyrics?> = _lyrics.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _sleepTimerActive = MutableStateFlow(false)
    val sleepTimerActive: StateFlow<Boolean> = _sleepTimerActive.asStateFlow()

    private val _sleepTimerEndTime = MutableStateFlow(0L)
    val sleepTimerEndTime: StateFlow<Long> = _sleepTimerEndTime.asStateFlow()

    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    val isFavorite: StateFlow<Boolean> = _currentSong.map { it?.isFavorite == true }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    init {
        val sessionToken = SessionToken(context, ComponentName(context, TuneoraPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            
            // Observe preferences
            scope.launch {
                preferencesRepository.applicationPreferences.collect { prefs ->
                    mediaController?.setPlaybackParameters(
                        PlaybackParameters(prefs.playbackSpeed, prefs.playbackPitch)
                    )
                }
            }

            // Periodically update position
            scope.launch {
                while (true) {
                    mediaController?.let {
                        _position.value = it.currentPosition
                        val dur = it.duration
                        _duration.value = if (dur > 0) dur else 0L
                    }
                    kotlinx.coroutines.delay(1000)
                }
            }

            mediaController?.let {
                _shuffleMode.value = it.shuffleModeEnabled
                _repeatMode.value = it.repeatMode
            }

            mediaController?.addListener(object : Player.Listener {

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    // Update duration when track changes
                    mediaController?.let { controller ->
                        val dur = controller.duration
                        _duration.value = if (dur > 0) dur else 0L
                        _lyrics.value = null
                        fetchLyrics()
                        
                        // Update current song state
                        val mediaId = mediaItem?.mediaId
                        if (mediaId != null) {
                            _currentSong.value = _queue.value.find { it.id.toString() == mediaId }
                        }
                    }
                    
                    if (_sleepTimerActive.value && _sleepTimerEndTime.value == -1L) {
                        pause()
                        cancelSleepTimer()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _shuffleMode.value = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.util.Log.e("PlaybackManager", "Player error: ${error.message}", error)
                    // Try to recover if it's a source error
                    if (error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
                        // Skip to next?
                        skipToNext()
                    }
                }
            })
        }, MoreExecutors.directExecutor())
    }

    fun playUris(uris: List<android.net.Uri>) {
        mediaController?.let { controller ->
            val mediaItems = uris.map { uri ->
                MediaItem.Builder()
                    .setUri(uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(uri.lastPathSegment ?: "Unknown")
                            .build()
                    )
                    .build()
            }
            controller.setMediaItems(mediaItems)
            controller.prepare()
            controller.play()
            
            // For external URIs, we don't have a Song object readily available with all metadata
            // We can create a dummy Song or update currentSong state appropriately
            _currentSong.value = Song(
                id = -1,
                title = uris.firstOrNull()?.lastPathSegment ?: "Unknown",
                artist = "Unknown",
                album = "Unknown",
                duration = 0,
                uri = uris.first(),
                albumId = -1,
                trackNumber = 0,
                year = 0,
                path = uris.first().toString()
            )
        }
    }

    fun playNext(song: Song) {
        mediaController?.let { controller ->
            val index = if (controller.nextMediaItemIndex == -1) controller.currentMediaItemIndex + 1 else controller.nextMediaItemIndex
            controller.addMediaItem(index, createMediaItem(song))
            _queue.value = _queue.value.toMutableList().apply { add(index.coerceIn(0, size), song) }
        }
    }

    fun addToQueue(song: Song) {
        mediaController?.let { controller ->
            controller.addMediaItem(createMediaItem(song))
            _queue.value = _queue.value + song
        }
    }

    fun playSong(song: Song, songs: List<Song> = emptyList()) {
        mediaController?.let {
            val mediaItems = if (songs.isEmpty()) {
                listOf(createMediaItem(song))
            } else {
                songs.map { createMediaItem(it) }
            }
            
            val startIndex = if (songs.isEmpty()) 0 else songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            
            it.setMediaItems(mediaItems, startIndex, 0L)
            
            // Auto-replay single tracks
            if (preferencesRepository.applicationPreferences.value.autoReplaySingleTracks && mediaItems.size == 1) {
                it.repeatMode = Player.REPEAT_MODE_ONE
            } else if (it.repeatMode == Player.REPEAT_MODE_ONE && mediaItems.size > 1) {
                // If it was REPEAT_ONE but we are playing a list, maybe revert to REPEAT_ALL or REPEAT_OFF?
                // For now, let's just leave it as is or reset if it was auto-set.
            }

            it.prepare()
            it.play()
            _currentSong.value = song
            _queue.value = songs.ifEmpty { listOf(song) }
        }
    }

    private fun createMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setArtworkUri(song.artworkUri)
                    .build()
            )
            .build()
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun toggleShuffle() {
        mediaController?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }

    fun toggleRepeat() {
        mediaController?.let {
            val nextMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                else -> Player.REPEAT_MODE_OFF
            }
            it.repeatMode = nextMode
        }
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun playQueueItem(song: Song) {
        mediaController?.let { controller ->
            val index = _queue.value.indexOfFirst { it.id == song.id }
            if (index != -1) {
                controller.seekToDefaultPosition(index)
                controller.play()
                _currentSong.value = song
            }
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        mediaController?.let { controller ->
            controller.moveMediaItem(from, to)
            _queue.value = _queue.value.toMutableList().apply {
                val item = removeAt(from)
                add(to, item)
            }
        }
    }

    fun removeQueueItem(index: Int) {
        mediaController?.let { controller ->
            controller.removeMediaItem(index)
            _queue.value = _queue.value.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    fun toggleFavorite(song: Song) {
        scope.launch {
            val newFavorite = !song.isFavorite
            musicRepository.toggleFavorite(song.id, newFavorite)
            
            // Update queue to ensure track changes reflect correct favorite state
            _queue.value = _queue.value.map { 
                if (it.id == song.id) it.copy(isFavorite = newFavorite) else it 
            }
            
            // Update local state if it's the current song
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = _currentSong.value?.copy(isFavorite = newFavorite)
            }
        }
    }

    fun setSleepTimer(durationMs: Long?) {
        cancelSleepTimer()
        _sleepTimerActive.value = true
        
        if (durationMs != null) {
            _sleepTimerEndTime.value = System.currentTimeMillis() + durationMs
            sleepTimerJob = scope.launch {
                kotlinx.coroutines.delay(durationMs)
                pause()
                cancelSleepTimer()
            }
        } else {
            // End of song logic is handled in onMediaItemTransition
            _sleepTimerEndTime.value = -1L // Marker for "End of Song"
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerActive.value = false
        _sleepTimerEndTime.value = 0L
    }

    fun pause() {
        mediaController?.pause()
    }

    fun refreshQueue() {
        mediaController?.let { controller ->
            val items = mutableListOf<Song>()
            for (i in 0 until controller.mediaItemCount) {
                val mediaId = controller.getMediaItemAt(i).mediaId
                // We should ideally fetch the latest song state from repository
                // but for now we just filter out deleted ones if we have their IDs
            }
            // For now, let's just trigger a re-sync or something
            scope.launch {
                musicRepository.refreshLibrary()
            }
        }
    }

    private fun fetchLyrics() {
        mediaController?.let { controller ->
            val command = SessionCommand("get_lyrics", Bundle.EMPTY)
            val future = controller.sendCustomCommand(command, Bundle.EMPTY)
            future.addListener({
                val result = future.get()
                if (result.resultCode == SessionResult.RESULT_SUCCESS) {
                    val lyricsBundle = result.extras
                    @Suppress("DEPRECATION")
                    val lyrics = lyricsBundle.getParcelable<SemanticLyrics>("lyrics")
                    _lyrics.value = lyrics
                }
            }, MoreExecutors.directExecutor())
        }
    }
}


