package com.maheswara660.tuneora.core.common.model

data class ApplicationPreferences(
    val themeConfig: ThemeConfig = ThemeConfig.SYSTEM,
    val useHighContrastDarkTheme: Boolean = false,
    val accentColorIndex: Int = -1, // -1 for Material You
    val sortBy: Sort.By = Sort.By.TITLE,
    val sortOrder: Sort.Order = Sort.Order.ASCENDING,
    val skipSilence: Boolean = false,
    val audioNormalization: Boolean = false,
    val sleepTimerDurationMinutes: Int = 0, // 0 for disabled
    val filterShortTracks: Boolean = true,
    val minTrackDurationSeconds: Int = 30,
    val artworkBlurIntensity: Float = 0.5f,
    val useGridLayout: Boolean = false,
    val autoReplaySingleTracks: Boolean = true,
    
    // Advanced Settings
    val playbackSpeed: Float = 1.0f,
    val playbackPitch: Float = 1.0f,
    val enableGlowLyrics: Boolean = true,
    val lyricsTextSize: Float = 18f,
    val doubleTapToSkipSeconds: Int = 10,
    val miniPlayerSwipeToSkip: Boolean = true,
    val animationSpeedMultiplier: Float = 1.0f,
    val lastFmScrobbling: Boolean = false,
    
    // Deep Porting
    val cornerRadius: Int = 12, // Default 12dp
    val crossfadeDurationMs: Int = 0, // 0 for disabled
    val artworkQuality: String = "High", // "Low", "High"
    val pauseOnUnplug: Boolean = true,
    
    // Media Library
    val markLastPlayedMedia: Boolean = true,
    val manageFolders: Set<String> = emptySet(),
    
    // Decoder
    val decoderPriority: DecoderPriority = DecoderPriority.HARDWARE_FIRST,
    
    // Audio
    val requireAudioFocus: Boolean = true,
    val pauseOnHeadsetDisconnect: Boolean = true,
    val showSystemVolumePanel: Boolean = false,
    val enableVolumeBoost: Boolean = false,
    
    // Additional Settings
    val useDynamicColors: Boolean = false,
    val useSquigglyProgress: Boolean = false,
    val showAudioQuality: Boolean = false,
    val albumArtCornerRadius: Int = 16,
    val blacklistedFolders: Set<String> = emptySet(),
    val autoplayOnLaunch: Boolean = false,
    val stopOnDismiss: Boolean = false,
    val fastDelete: Boolean = false,
)

enum class DecoderPriority {
    HARDWARE_FIRST,
    SOFTWARE_FIRST
}

