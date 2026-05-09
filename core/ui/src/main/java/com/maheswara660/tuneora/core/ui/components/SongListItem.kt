package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.common.model.Song

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    isSelected: Boolean = false,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false
) {
    TuneoraSegmentedListItem(
        modifier = Modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        selected = isSelected,
        isSegmented = true,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        leadingContent = {
            Box(contentAlignment = Alignment.Center) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    TuneoraAlbumArt(
                        model = song.artworkUri,
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        },
        content = {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = song.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
