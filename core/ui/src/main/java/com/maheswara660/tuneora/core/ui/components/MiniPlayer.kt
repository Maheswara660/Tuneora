package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.maheswara660.tuneora.core.common.model.Song
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.roundToInt
import com.maheswara660.tuneora.core.ui.theme.LocalApplicationPreferences

@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onClick: () -> Unit,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    isFavorite: Boolean = false,
    progress: Float = 0f,
    swipeEnabled: Boolean = true,
    isFloating: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    val preferences = LocalApplicationPreferences.current
    val cornerRadius = preferences.cornerRadius.dp
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val coroutineScope = rememberCoroutineScope()
    val offsetXAnimatable = remember { Animatable(0f) }
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val strokeWidth = 3.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                .clip(RoundedCornerShape(36.dp))
                .clickable { onClick() }
                .pointerInput(swipeEnabled) {
                    if (swipeEnabled) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (offsetXAnimatable.value > 100) {
                                        onPrevious()
                                    } else if (offsetXAnimatable.value < -100) {
                                        onNext()
                                    }
                                    offsetXAnimatable.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                coroutineScope.launch {
                                    offsetXAnimatable.snapTo(offsetXAnimatable.value + dragAmount)
                                }
                                change.consume()
                            }
                        )
                    }
                },
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(36.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play button with circular progress
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .drawWithContent {
                            drawContent()
                            val stroke = Stroke(
                                width = strokeWidth.toPx(), 
                                cap = StrokeCap.Round
                            )
                            val startAngle = -90f
                            val sweepAngle = 360f * progress
                            val diameter = size.minDimension
                            val topLeft = Offset(
                                (size.width - diameter) / 2, 
                                (size.height - diameter) / 2
                            )

                            // Draw track
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = stroke,
                            )
                            // Draw progress
                            drawArc(
                                color = primaryColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = stroke,
                            )
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onTogglePlayPause() }
                    ) {
                        TuneoraAlbumArt(
                            model = song.artworkUri,
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape
                        )
                        
                        // Overlay when paused or small play/pause icon could be added here
                        // For now, let's keep it clean like Metrolist
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                        )
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Favorite Button
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Next Button
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
