package com.maheswara660.tuneora.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SquigglySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "squiggly")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val waveLength = 40.dp
    val amplitude = 4.dp
    val strokeWidth = 3.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures { offset ->
                        val newValue = (offset.x / size.width) * (valueRange.endInclusive - valueRange.start) + valueRange.start
                        onValueChange(newValue.coerceIn(valueRange))
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val progressX = (value / (valueRange.endInclusive - valueRange.start)) * width
        
        val waveLengthPx = waveLength.toPx()
        val amplitudePx = amplitude.toPx()
        val strokeWidthPx = strokeWidth.toPx()

        // Draw background line (inactive)
        drawLine(
            color = color.copy(alpha = 0.24f),
            start = Offset(progressX, centerY),
            end = Offset(width, centerY),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )

        // Draw squiggly line (active)
        val path = Path()
        val points = 100
        for (i in 0..points) {
            val x = (i.toFloat() / points) * progressX
            val relativeX = x / waveLengthPx * 2 * PI.toFloat()
            val y = centerY + sin(relativeX + phase) * amplitudePx
            
            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
        
        // Draw thumb
        drawCircle(
            color = color,
            radius = 6.dp.toPx(),
            center = Offset(progressX, centerY + sin(progressX / waveLengthPx * 2 * PI.toFloat() + phase) * amplitudePx)
        )
    }
}
