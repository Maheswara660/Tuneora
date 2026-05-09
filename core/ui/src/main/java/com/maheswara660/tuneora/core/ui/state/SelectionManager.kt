package com.maheswara660.tuneora.core.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.maheswara660.tuneora.core.common.model.Song
import java.io.Serializable

@Composable
fun rememberSelectionManager(): SelectionManager {
    return rememberSaveable(saver = SelectionManager.Saver) {
        SelectionManager()
    }
}

@Stable
class SelectionManager(
    initialSelectedSongs: Set<SelectedSong> = emptySet(),
    initialIsInSelectionMode: Boolean = false,
) {
    var selectedSongs: Set<SelectedSong> by mutableStateOf(initialSelectedSongs)
        private set

    var isInSelectionMode: Boolean by mutableStateOf(initialIsInSelectionMode)
        private set

    val selectedCount: Int by derivedStateOf { selectedSongs.size }

    val isSingleSongSelected: Boolean by derivedStateOf { selectedSongs.size == 1 }

    fun toggleSongSelection(song: Song) {
        val selectedSong = selectedSongs.find { it.id == song.id }
        selectedSongs = if (selectedSong != null) {
            selectedSongs - selectedSong
        } else {
            selectedSongs + song.toSelectedSong()
        }
        
        if (selectedSongs.isNotEmpty()) {
            enterSelectionMode()
        } else {
            exitSelectionMode()
        }
    }

    fun selectSong(song: Song) {
        enterSelectionMode()
        selectedSongs = selectedSongs + song.toSelectedSong()
    }

    fun clearSelection() {
        selectedSongs = emptySet()
    }

    fun enterSelectionMode() {
        isInSelectionMode = true
    }

    fun exitSelectionMode() {
        isInSelectionMode = false
        selectedSongs = emptySet()
    }

    fun isSongSelected(song: Song): Boolean {
        return selectedSongs.find { it.id == song.id } != null
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        val Saver = Saver<SelectionManager, Map<String, Any>>(
            save = {
                mapOf(
                    "selectedSongs" to it.selectedSongs,
                    "isInSelectionMode" to it.isInSelectionMode,
                )
            },
            restore = {
                SelectionManager(
                    initialSelectedSongs = (it["selectedSongs"] as? Set<SelectedSong>) ?: emptySet(),
                    initialIsInSelectionMode = it["isInSelectionMode"] as? Boolean ?: false,
                )
            },
        )
    }
}

@Stable
data class SelectedSong(
    val id: Long,
    val title: String,
    val uriString: String,
) : Serializable

private fun Song.toSelectedSong() = SelectedSong(
    id = id,
    title = title,
    uriString = uri.toString(),
)
