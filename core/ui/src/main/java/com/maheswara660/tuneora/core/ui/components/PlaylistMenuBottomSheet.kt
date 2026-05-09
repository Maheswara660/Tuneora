package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.common.model.Playlist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistMenuBottomSheet(
    playlist: Playlist,
    onDismissRequest: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    TuneoraBottomSheet(
        onDismissRequest = onDismissRequest,
        title = playlist.name,
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
            Triple(Icons.Rounded.PlayArrow, "Play", onPlay),
            Triple(Icons.Rounded.Shuffle, "Shuffle", onShuffle),
            Triple(Icons.Rounded.Edit, "Edit playlist name", onRename),
            Triple(Icons.Rounded.Delete, "Delete playlist", onDelete)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            menuItems.forEachIndexed { index, (icon, label, action) ->
                val isDelete = label == "Delete playlist"
                TuneoraSegmentedListItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                        action()
                        onDismissRequest()
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
