package com.maheswara660.tuneora.feature.player.screens
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.ui.components.TuneoraAlbumArt
import com.maheswara660.tuneora.core.ui.components.TuneoraBottomSheet
import com.maheswara660.tuneora.core.ui.components.TuneoraSegmentedListItem
import com.maheswara660.tuneora.core.ui.theme.LocalApplicationPreferences
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QueueBottomSheet(
    queue: List<Song>,
    currentSong: Song?,
    onDismissRequest: () -> Unit,
    onSongClick: (Song) -> Unit,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Auto-scroll to current item
    LaunchedEffect(Unit) {
        val currentIndex = queue.indexOfFirst { it.id == currentSong?.id }
        if (currentIndex != -1) {
            lazyListState.scrollToItem(currentIndex)
        }
    }

    TuneoraBottomSheet(
        onDismissRequest = onDismissRequest,
        title = "Playing Queue",
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    ) {
        if (queue.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Queue is empty", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f),
                state = lazyListState,
                contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(
                    items = queue,
                    key = { _, song -> song.id }
                ) { index, song ->
                    ReorderableItem(reorderableLazyListState, key = song.id) {
                        val isCurrent = song.id == currentSong?.id
                        val interactionSource = remember { MutableInteractionSource() }

                        TuneoraSegmentedListItem(
                            modifier = Modifier.draggableHandle(
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                interactionSource = interactionSource,
                                dragGestureDetector = DragGestureDetector.LongPress
                            ),
                            onClick = { onSongClick(song) },
                            selected = isCurrent,
                            isFirstItem = index == 0,
                            isLastItem = index == queue.lastIndex,
                            interactionSource = interactionSource,
                            leadingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.DragHandle,
                                        contentDescription = "Reorder",
                                        tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    TuneoraAlbumArt(
                                        model = song.artworkUri,
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            },
                            content = {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingContent = {
                                if (isCurrent) {
                                    Icon(
                                        Icons.Rounded.Equalizer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    IconButton(onClick = { onRemove(index) }) {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
