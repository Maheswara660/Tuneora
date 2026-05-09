package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.common.model.Playlist
import com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPlaylistBottomSheet(
    playlists: List<Playlist>,
    onDismissRequest: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    TuneoraBottomSheet(
        onDismissRequest = onDismissRequest,
        title = "Add to playlist",
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(playlists.size) { index ->
                val playlist = playlists[index]
                TuneoraSegmentedListItem(
                    onClick = {
                        onPlaylistSelected(playlist)
                        onDismissRequest()
                    },
                    isFirstItem = index == 0,
                    isLastItem = index == playlists.lastIndex,
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    content = { Text(playlist.name) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                TuneoraSegmentedListItem(
                    onClick = {
                        onCreateNewPlaylist()
                    },
                    isFirstItem = true,
                    isLastItem = true,
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(TuneoraIcons.Add, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    },
                    content = { Text("Create new playlist", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) }
                )
            }
        }
    }
}
