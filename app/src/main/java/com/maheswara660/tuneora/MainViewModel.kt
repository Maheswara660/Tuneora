package com.maheswara660.tuneora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import com.maheswara660.tuneora.core.media.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val playbackManager: PlaybackManager,
    private val preferencesRepository: PreferencesRepository,
    private val repository: com.maheswara660.tuneora.core.data.repository.MusicRepository,
    private val folderRepository: com.maheswara660.tuneora.core.data.repository.FolderRepository
) : ViewModel() {
    val currentSong = playbackManager.currentSong
    val isPlaying = playbackManager.isPlaying

    fun toggleFavorite(song: com.maheswara660.tuneora.core.common.model.Song) {
        playbackManager.toggleFavorite(song)
    }

    val uiState: StateFlow<ApplicationPreferences> = preferencesRepository.applicationPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ApplicationPreferences()
        )

    fun togglePlayPause() {
        playbackManager.togglePlayPause()
    }

    fun handleIntent(intent: android.content.Intent) {
        when (intent.action) {
            android.content.Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    playbackManager.playUris(listOf(uri))
                }
            }
            android.content.Intent.ACTION_SEND -> {
                val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
                }
                uri?.let { playbackManager.playUris(listOf(it)) }
            }
            android.content.Intent.ACTION_SEND_MULTIPLE -> {
                val uris = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
                }
                uris?.let { playbackManager.playUris(it) }
            }
        }
    }
}
