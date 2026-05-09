package com.maheswara660.tuneora.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val preferencesRepository: com.maheswara660.tuneora.core.data.repository.PreferencesRepository,
    private val musicRepository: com.maheswara660.tuneora.core.data.repository.MusicRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {
    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    val songs: StateFlow<List<Song>> = playlistRepository.getSongsForPlaylist(playlistId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val preferences = preferencesRepository.applicationPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.maheswara660.tuneora.core.common.model.ApplicationPreferences()
        )

    fun updatePreferences(preferences: com.maheswara660.tuneora.core.common.model.ApplicationPreferences) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences { preferences }
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            musicRepository.refreshLibrary()
        }
    }

    fun getPlaylists() = playlistRepository.getAllPlaylists()

    fun addSongToPlaylist(song: Song, playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, song.id)
        }
    }

    fun createPlaylistAndAddSong(song: Song, name: String) {
        viewModelScope.launch {
            val id = playlistRepository.createPlaylist(name)
            playlistRepository.addSongToPlaylist(id, song.id)
        }
    }
}
