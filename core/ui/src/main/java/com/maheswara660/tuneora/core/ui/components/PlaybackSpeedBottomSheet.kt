package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedBottomSheet(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    onDismissRequest: () -> Unit
) {
    TuneoraBottomSheet(
        onDismissRequest = onDismissRequest,
        title = "Playback Speed",
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        },
        confirmButton = {
            Button(
                onClick = { onSpeedChange(1.0f) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to 1.0x")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${"%.2f".format(currentSpeed)}x",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Slider(
                value = currentSpeed,
                onValueChange = onSpeedChange,
                valueRange = 0.5f..2.5f,
                steps = 19, // 0.1 increments
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0.5x", style = MaterialTheme.typography.labelSmall)
                Text("1.0x", style = MaterialTheme.typography.labelSmall)
                Text("1.5x", style = MaterialTheme.typography.labelSmall)
                Text("2.0x", style = MaterialTheme.typography.labelSmall)
                Text("2.5x", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
