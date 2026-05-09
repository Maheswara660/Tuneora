package com.maheswara660.tuneora.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheswara660.tuneora.core.common.model.Playlist
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.common.model.SemanticLyrics
import com.maheswara660.tuneora.core.data.repository.MusicRepository
import com.maheswara660.tuneora.core.data.repository.PlaylistRepository
import com.maheswara660.tuneora.core.media.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playbackManager: PlaybackManager,
    private val playlistRepository: PlaylistRepository,
    private val musicRepository: MusicRepository,
    private val preferencesRepository: com.maheswara660.tuneora.core.data.repository.PreferencesRepository
) : ViewModel() {
    val currentSong: StateFlow<Song?> = playbackManager.currentSong
    val isPlaying: StateFlow<Boolean> = playbackManager.isPlaying
    
    fun deleteSong(context: android.content.Context, song: Song) {
        com.maheswara660.tuneora.core.ui.util.DeleteUtils.deleteSong(context, song) {
            // If it's the current song, skip to next
            if (currentSong.value?.id == song.id) {
                skipToNext()
            }
            playbackManager.refreshQueue() // Need to implement this
        }
    }
    val position: StateFlow<Long> = playbackManager.position
    val duration: StateFlow<Long> = playbackManager.duration
    val shuffleMode: StateFlow<Boolean> = playbackManager.shuffleMode
    val repeatMode: StateFlow<Int> = playbackManager.repeatMode
    val lyrics: StateFlow<SemanticLyrics?> = playbackManager.lyrics
    val queue: StateFlow<List<Song>> = playbackManager.queue
    val sleepTimerActive: StateFlow<Boolean> = playbackManager.sleepTimerActive
    val isFavorite: StateFlow<Boolean> = playbackManager.isFavorite
    val playbackSpeed: StateFlow<Float> = preferencesRepository.applicationPreferences.map { it.playbackSpeed }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    fun updatePlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences { it.copy(playbackSpeed = speed) }
        }
    }
    
    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePlayPause() {
        playbackManager.togglePlayPause()
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
    }

    fun toggleRepeat() {
        playbackManager.toggleRepeat()
    }

    fun toggleFavorite(song: Song) {
        playbackManager.toggleFavorite(song)
    }

    fun skipToNext() {
        playbackManager.skipToNext()
    }

    fun skipToPrevious() {
        playbackManager.skipToPrevious()
    }

    fun seekTo(position: Long) {
        playbackManager.seekTo(position)
    }

    fun addSongToPlaylist(song: Song, playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, song.id)
        }
    }

    fun createPlaylistAndAddSong(song: Song, playlistName: String) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(playlistName)
            playlistRepository.addSongToPlaylist(playlistId, song.id)
        }
    }

    fun playQueueItem(song: Song) {
        playbackManager.playQueueItem(song)
    }

    fun playNext(song: Song) {
        playbackManager.playNext(song)
    }

    fun addToQueue(song: Song) {
        playbackManager.addToQueue(song)
    }

    fun setSleepTimer(durationMs: Long?) {
        playbackManager.setSleepTimer(durationMs)
    }

    fun cancelSleepTimer() {
        playbackManager.cancelSleepTimer()
    }

    fun moveQueueItem(from: Int, to: Int) {
        playbackManager.moveQueueItem(from, to)
    }

    fun removeQueueItem(index: Int) {
        playbackManager.removeQueueItem(index)
    }
}

