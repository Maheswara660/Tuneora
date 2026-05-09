package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.maheswara660.tuneora.core.ui.theme.LocalApplicationPreferences

@Composable
fun TuneoraAlbumArt(
    modifier: Modifier = Modifier,
    model: Any?,
    contentDescription: String? = null,
    shape: Shape? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val preferences = LocalApplicationPreferences.current
    val cornerRadius = preferences.cornerRadius.dp
    val actualShape = shape ?: RoundedCornerShape(cornerRadius)

    Surface(
        modifier = modifier.clip(actualShape),
        shape = actualShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
            loading = {
                AlbumArtPlaceholder()
            },
            error = {
                AlbumArtPlaceholder()
            }
        )
    }
}

@Composable
private fun AlbumArtPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}
