package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TimerOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.ui.theme.LocalApplicationPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    onDismissRequest: () -> Unit,
    onTimerSelected: (Long?) -> Unit, // null means "End of song", Long is milliseconds
    onCancelTimer: () -> Unit,
    isTimerActive: Boolean
) {
    val options = listOf(
        "5 minutes" to 5 * 60 * 1000L,
        "10 minutes" to 10 * 60 * 1000L,
        "15 minutes" to 15 * 60 * 1000L,
        "30 minutes" to 30 * 60 * 1000L,
        "45 minutes" to 45 * 60 * 1000L,
        "60 minutes" to 60 * 60 * 1000L,
        "End of song" to null
    )

    TuneoraBottomSheet(
        onDismissRequest = onDismissRequest,
        title = "Sleep Timer",
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isTimerActive) {
                TuneoraSegmentedListItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                        onCancelTimer()
                        onDismissRequest()
                    },
                    leadingContent = { Icon(Icons.Rounded.TimerOff, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    content = { Text("Turn off timer", color = MaterialTheme.colorScheme.error) },
                    isFirstItem = true,
                    isLastItem = false
                )
            }

            options.forEachIndexed { index, (label, duration) ->
                TuneoraSegmentedListItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                        onTimerSelected(duration)
                        onDismissRequest()
                    },
                    leadingContent = { Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    content = { Text(label) },
                    isFirstItem = !isTimerActive && index == 0,
                    isLastItem = index == options.lastIndex
                )
            }
        }
    }
}
