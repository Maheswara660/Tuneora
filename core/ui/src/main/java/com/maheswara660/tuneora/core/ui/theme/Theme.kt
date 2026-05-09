package com.maheswara660.tuneora.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import com.maheswara660.tuneora.core.common.model.ThemeConfig
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalApplicationPreferences = staticCompositionLocalOf { ApplicationPreferences() }

private val DarkColorScheme = darkColorScheme(
    primary = TuneoraTeal,
    secondary = TuneoraTealSecondary,
    tertiary = TuneoraTealTertiary,
    background = Color(0xFF0E1414),
    surface = Color(0xFF0E1414),
    surfaceContainer = Color(0xFF161D1D),
    onBackground = Color(0xFFDEE3E3),
    onSurface = Color(0xFFDEE3E3)
)

private val LightColorScheme = lightColorScheme(
    primary = TuneoraTeal,
    secondary = TuneoraTealSecondary,
    tertiary = TuneoraTealTertiary,
    background = Color(0xFFF4FBFA),
    surface = Color(0xFFF4FBFA),
    surfaceContainer = Color(0xFFE9EFEE),
    onBackground = Color(0xFF161D1D),
    onSurface = Color(0xFF161D1D)
)

@Composable
fun TuneoraTheme(
    preferences: ApplicationPreferences = ApplicationPreferences(),
    content: @Composable () -> Unit
) {
    val darkTheme = when (preferences.themeConfig) {
        ThemeConfig.LIGHT -> false
        ThemeConfig.DARK -> true
        ThemeConfig.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val dynamicColor = preferences.accentColorIndex == -1
    
    var colorScheme = when {
        dynamicColor && supportsDynamicTheming() -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply custom accent if specified
    if (preferences.accentColorIndex >= 0) {
        val accent = CustomAccents.getOrNull(preferences.accentColorIndex)
        if (accent != null) {
            colorScheme = colorScheme.copy(
                primary = accent.primary,
                secondary = accent.secondary,
                outline = accent.primary.copy(alpha = 0.5f)
            )
        }
    }

    // Apply AMOLED black if specified
    if (darkTheme && preferences.useHighContrastDarkTheme) {
        colorScheme = colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceBright = Color(0xFF1A1A1A),
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerHigh = Color(0xFF121212),
            surfaceContainerHighest = Color(0xFF1A1A1A),
            surfaceVariant = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            onSurfaceVariant = Color.White,
            outline = Color.White.copy(alpha = 0.2f),
            outlineVariant = Color.White.copy(alpha = 0.1f),
        )
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as Activity).window
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalApplicationPreferences provides preferences) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamicTheming() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S