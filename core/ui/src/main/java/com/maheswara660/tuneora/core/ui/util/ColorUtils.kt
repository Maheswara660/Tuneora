package com.maheswara660.tuneora.core.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberDominantColor(
    context: Context,
    imageUri: Uri?,
    defaultColor: Color = Color.Gray
): Color {
    var dominantColor by remember { mutableStateOf(defaultColor) }

    LaunchedEffect(imageUri) {
        if (imageUri == null) {
            dominantColor = defaultColor
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUri)
                .allowHardware(false) // Required for Palette
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.image as? coil3.BitmapImage)?.bitmap
                if (bitmap != null) {
                    val palette = Palette.from(bitmap).generate()
                    val swatch = palette.vibrantSwatch ?: palette.mutedSwatch ?: palette.dominantSwatch
                    if (swatch != null) {
                        dominantColor = Color(swatch.rgb)
                    }
                }
            }
        }
    }

    return dominantColor
}
