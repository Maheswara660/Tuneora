package com.maheswara660.tuneora.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import com.maheswara660.tuneora.core.common.model.Folder
import com.maheswara660.tuneora.core.data.repository.FolderRepository
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.data.repository.MusicRepository
import com.maheswara660.tuneora.core.data.repository.PlaylistRepository
import com.maheswara660.tuneora.core.common.model.Playlist
import kotlinx.coroutines.flow.map
import com.maheswara660.tuneora.core.media.PlaybackManager

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val musicRepository: MusicRepository,
    private val preferencesRepository: PreferencesRepository,
    private val playlistRepository: PlaylistRepository,
    val playbackManager: PlaybackManager
) : ViewModel() {
    fun getSongsForPlaylist(playlistId: Long) = playlistRepository.getSongsForPlaylist(playlistId)
    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val songs: StateFlow<List<Song>> = musicRepository.songs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val albums: StateFlow<List<String>> = musicRepository.songs
        .map { songs -> songs.map { it.album }.distinct().sorted() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val artists: StateFlow<List<String>> = musicRepository.songs
        .map { songs -> songs.map { it.artist }.distinct().sorted() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentlyAdded: StateFlow<List<Song>> = musicRepository.songs
        .map { songs -> songs.sortedByDescending { it.dateAdded } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val history: StateFlow<List<Song>> = musicRepository.songs
        .map { songs -> songs.filter { it.lastPlayed > 0 }.sortedByDescending { it.lastPlayed } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val folders: StateFlow<List<Folder>> = folderRepository.directories

        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteSongs: StateFlow<List<Song>> = musicRepository.getFavoriteSongs()
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

    fun updatePreferences(preferences: ApplicationPreferences) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences { preferences }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
        }
    }

    fun renamePlaylist(playlistId: Long, name: String) {
        viewModelScope.launch {
            playlistRepository.renamePlaylist(playlistId, name)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun createPlaylistAndAddSong(song: Song, playlistName: String) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(playlistName)
            playlistRepository.addSongToPlaylist(playlistId, song.id)
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            musicRepository.refreshLibrary()
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            musicRepository.deleteSongsPermanently(listOf(song))
        }
    }
}
