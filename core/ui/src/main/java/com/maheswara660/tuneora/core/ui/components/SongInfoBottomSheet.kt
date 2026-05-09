package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.common.model.Song
import com.maheswara660.tuneora.core.ui.theme.LocalApplicationPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongInfoBottomSheet(
    song: Song,
    onDismissRequest: () -> Unit
) {
    val preferences = LocalApplicationPreferences.current
    val cornerRadius = preferences.cornerRadius.dp
    
    TuneoraBottomSheet(
        onDismissRequest = onDismissRequest,
        title = "Song Information",
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    ) {
        val context = LocalContext.current
        val infoItems = listOf(
            "Title" to song.title,
            "Artist" to song.artist,
            "Album" to song.album,
            "Duration" to formatDuration(song.duration),
            "Year" to if (song.year > 0) song.year.toString() else "Unknown",
            "Track Number" to if (song.trackNumber > 0) song.trackNumber.toString() else "Unknown",
            "File Path" to song.path,
            "Date Added" to formatDate(song.dateAdded)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            infoItems.forEachIndexed { index, (label, value) ->
                TuneoraSegmentedListItem(
                    isFirstItem = index == 0,
                    isLastItem = index == infoItems.lastIndex,
                    onClick = {},
                    content = {
                        Column {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, song.uri)
                        putExtra(Intent.EXTRA_TITLE, song.title)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Song"))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Song")
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60
    return "%d:%02d".format(minutes, seconds)
}

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Unknown"
    val date = Date(timestamp * 1000) // MediaStore uses seconds
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}
