package com.maheswara660.tuneora.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.maheswara660.tuneora.core.data.repository.PlaylistRepository
import com.maheswara660.tuneora.core.common.model.Playlist

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val musicRepository: com.maheswara660.tuneora.core.data.repository.MusicRepository,
    private val preferencesRepository: PreferencesRepository,
    private val playlistRepository: PlaylistRepository,
    private val scanner: com.maheswara660.tuneora.core.media.TuneoraScanner
) : ViewModel() {
    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val history: StateFlow<List<Song>> = musicRepository.getHistorySongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val preferences: StateFlow<ApplicationPreferences> = preferencesRepository.applicationPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ApplicationPreferences()
        )

    fun rescanLibrary() {
        viewModelScope.launch {
            val scannedSongs = scanner.scanMusic()
            if (scannedSongs.isNotEmpty()) {
                musicRepository.deleteAllSongs()
                musicRepository.insertSongs(scannedSongs)
            }
        }
    }

    fun updatePreferences(preferences: ApplicationPreferences) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences { preferences }
        }
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
}
