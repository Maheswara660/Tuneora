package com.maheswara660.tuneora.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import com.maheswara660.tuneora.core.data.repository.MusicRepository
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import com.maheswara660.tuneora.core.data.repository.PlaylistRepository
import com.maheswara660.tuneora.core.common.model.Playlist
import com.maheswara660.tuneora.core.media.TuneoraScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val preferencesRepository: PreferencesRepository,
    private val playlistRepository: PlaylistRepository,
    private val scanner: TuneoraScanner
) : ViewModel() {

    val songs: StateFlow<List<Song>> = repository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val preferences: StateFlow<ApplicationPreferences> = preferencesRepository.applicationPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApplicationPreferences())

    fun rescanLibrary() {

        viewModelScope.launch {
            val scannedSongs = scanner.scanMusic()
            if (scannedSongs.isNotEmpty()) {
                repository.deleteAllSongs()
                repository.insertSongs(scannedSongs)
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

    fun deleteSongs(songsToDelete: List<Song>) {
        viewModelScope.launch {
            repository.deleteSongsPermanently(songsToDelete)
        }
    }
}

