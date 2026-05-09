package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.common.model.Playlist
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongMenuBottomSheet(
    song: Song,
    onDismissRequest: () -> Unit,
    onPlayNext: () -> Unit = {},
    onAddToQueue: () -> Unit = {},
    onAddToPlaylist: (Song, Long) -> Unit = { _, _ -> },
    onCreatePlaylistAndAdd: (Song, String) -> Unit = { _, _ -> },
    playlists: List<Playlist> = emptyList(),
    onGoToAlbum: (String) -> Unit = {},
    onGoToArtist: (String) -> Unit = {},
    onShowDetails: (Song) -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val context = LocalContext.current
    var showSelectPlaylist by remember { mutableStateOf(false) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteConfirmationBottomSheet(
            songs = listOf(song),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete()
                showDeleteDialog = false
                onDismissRequest()
            }
        )
    }

    if (showInfoSheet) {
        SongInfoBottomSheet(
            song = song,
            onDismissRequest = { showInfoSheet = false }
        )
    }

    if (showSelectPlaylist) {
        SelectPlaylistBottomSheet(
            playlists = playlists,
            onDismissRequest = { showSelectPlaylist = false },
            onPlaylistSelected = { playlist ->
                onAddToPlaylist(song, playlist.id)
                onDismissRequest()
            },
            onCreateNewPlaylist = {
                showSelectPlaylist = false
                showCreatePlaylist = true
            }
        )
    }

    if (showCreatePlaylist) {
        CreatePlaylistBottomSheet(
            onDismiss = { showCreatePlaylist = false },
            onCreate = { name ->
                onCreatePlaylistAndAdd(song, name)
                onDismissRequest()
            }
        )
    }

    TuneoraBottomSheet(
        onDismissRequest = onDismissRequest,
        title = song.title,
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    ) {
        val menuItems = listOf(
            Triple(Icons.AutoMirrored.Rounded.PlaylistPlay, "Play next", onPlayNext),
            Triple(Icons.AutoMirrored.Rounded.QueueMusic, "Add to queue", onAddToQueue),
            Triple(Icons.AutoMirrored.Rounded.PlaylistAdd, "Add to playlist", {
                if (playlists.isEmpty()) {
                    showCreatePlaylist = true
                } else {
                    showSelectPlaylist = true
                }
            }),
            Triple(Icons.Rounded.Album, "Go to album", { onGoToAlbum(song.album) }),
            Triple(Icons.Rounded.Person, "Go to artist", { onGoToArtist(song.artist) }),
            Triple(Icons.Rounded.Share, "Share", {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, song.uri)
                    putExtra(Intent.EXTRA_TITLE, song.title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Song"))
            }),
            Triple(Icons.Rounded.Info, "Song information", { showInfoSheet = true }),
            Triple(Icons.Rounded.Delete, "Delete from device", { showDeleteDialog = true })
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            menuItems.forEachIndexed { index, (icon, label, action) ->
                val isDelete = label == "Delete from device"
                TuneoraSegmentedListItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                        action()
                        if (!isDelete && label != "Add to playlist" && label != "Song information") {
                            onDismissRequest()
                        }
                    },
                    leadingContent = { 
                        Icon(
                            icon, 
                            contentDescription = null,
                            tint = if (isDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ) 
                    },
                    content = { 
                        Text(
                            text = label,
                            color = if (isDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    isFirstItem = index == 0,
                    isLastItem = index == menuItems.lastIndex
                )
            }
        }
    }
}
