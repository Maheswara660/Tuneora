package com.maheswara660.tuneora.core.data.repository

import android.content.Context
import com.maheswara660.tuneora.core.common.ApplicationScope
import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import com.maheswara660.tuneora.core.common.model.DecoderPriority
import com.maheswara660.tuneora.core.common.model.Sort
import com.maheswara660.tuneora.core.common.model.ThemeConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : PreferencesRepository {

    private val sharedPreferences = context.getSharedPreferences("tuneora_prefs", Context.MODE_PRIVATE)

    private val _applicationPreferences = MutableStateFlow(loadPreferences())
    override val applicationPreferences: StateFlow<ApplicationPreferences> = _applicationPreferences.asStateFlow()

    private fun loadPreferences(): ApplicationPreferences {
        val themeString = sharedPreferences.getString("theme", ThemeConfig.SYSTEM.name)
        val themeConfig = try {
            ThemeConfig.valueOf(themeString ?: ThemeConfig.SYSTEM.name)
        } catch (e: Exception) {
            ThemeConfig.SYSTEM
        }

        val sortByString = sharedPreferences.getString("sort_by", Sort.By.TITLE.name)
        val sortBy = try {
            Sort.By.valueOf(sortByString ?: Sort.By.TITLE.name)
        } catch (e: Exception) {
            Sort.By.TITLE
        }

        val sortOrderString = sharedPreferences.getString("sort_order", Sort.Order.ASCENDING.name)
        val sortOrder = try {
            Sort.Order.valueOf(sortOrderString ?: Sort.Order.ASCENDING.name)
        } catch (e: Exception) {
            Sort.Order.ASCENDING
        }

        return ApplicationPreferences(
            themeConfig = themeConfig,
            useHighContrastDarkTheme = sharedPreferences.getBoolean("high_contrast", false),
            accentColorIndex = sharedPreferences.getInt("accent_index", -1),
            sortBy = sortBy,
            sortOrder = sortOrder,
            skipSilence = sharedPreferences.getBoolean("skip_silence", false),
            audioNormalization = sharedPreferences.getBoolean("audio_normalization", false),
            sleepTimerDurationMinutes = sharedPreferences.getInt("sleep_timer", 0),
            filterShortTracks = sharedPreferences.getBoolean("filter_short", true),
            minTrackDurationSeconds = sharedPreferences.getInt("min_duration", 30),
            artworkBlurIntensity = sharedPreferences.getFloat("blur_intensity", 0.5f),
            useGridLayout = sharedPreferences.getBoolean("grid_layout", true),
            autoReplaySingleTracks = sharedPreferences.getBoolean("auto_replay", true),
            playbackSpeed = sharedPreferences.getFloat("playback_speed", 1.0f),
            playbackPitch = sharedPreferences.getFloat("playback_pitch", 1.0f),
            enableGlowLyrics = sharedPreferences.getBoolean("glow_lyrics", true),
            lyricsTextSize = sharedPreferences.getFloat("lyrics_size", 18f),
            doubleTapToSkipSeconds = sharedPreferences.getInt("skip_seconds", 10),
            miniPlayerSwipeToSkip = sharedPreferences.getBoolean("mini_swipe", true),
            animationSpeedMultiplier = sharedPreferences.getFloat("anim_speed", 1.0f),
            lastFmScrobbling = sharedPreferences.getBoolean("scrobbling", false),
            cornerRadius = sharedPreferences.getInt("corner_radius", 12),
            crossfadeDurationMs = sharedPreferences.getInt("crossfade_duration", 0),
            artworkQuality = sharedPreferences.getString("artwork_quality", "High") ?: "High",
            pauseOnUnplug = sharedPreferences.getBoolean("pause_on_unplug", true),
            markLastPlayedMedia = sharedPreferences.getBoolean("mark_last_played", true),
            manageFolders = sharedPreferences.getStringSet("manage_folders", emptySet()) ?: emptySet(),
            decoderPriority = DecoderPriority.valueOf(sharedPreferences.getString("decoder_priority", DecoderPriority.HARDWARE_FIRST.name) ?: DecoderPriority.HARDWARE_FIRST.name),
            requireAudioFocus = sharedPreferences.getBoolean("require_audio_focus", true),
            pauseOnHeadsetDisconnect = sharedPreferences.getBoolean("pause_on_headset", true),
            showSystemVolumePanel = sharedPreferences.getBoolean("show_volume_panel", false),
            enableVolumeBoost = sharedPreferences.getBoolean("volume_boost", false),
        )
    }



    override suspend fun updateApplicationPreferences(transform: (ApplicationPreferences) -> ApplicationPreferences) {
        val newPrefs = transform(_applicationPreferences.value)
        _applicationPreferences.value = newPrefs
        savePreferences(newPrefs)
    }

    private fun savePreferences(prefs: ApplicationPreferences) {
        sharedPreferences.edit().apply {
            putString("theme", prefs.themeConfig.name)
            putBoolean("high_contrast", prefs.useHighContrastDarkTheme)
            putInt("accent_index", prefs.accentColorIndex)
            putString("sort_by", prefs.sortBy.name)
            putString("sort_order", prefs.sortOrder.name)
            putBoolean("skip_silence", prefs.skipSilence)
            putBoolean("audio_normalization", prefs.audioNormalization)
            putInt("sleep_timer", prefs.sleepTimerDurationMinutes)
            putBoolean("filter_short", prefs.filterShortTracks)
            putInt("min_duration", prefs.minTrackDurationSeconds)
            putFloat("blur_intensity", prefs.artworkBlurIntensity)
            putBoolean("grid_layout", prefs.useGridLayout)
            putBoolean("auto_replay", prefs.autoReplaySingleTracks)
            putFloat("playback_speed", prefs.playbackSpeed)
            putFloat("playback_pitch", prefs.playbackPitch)
            putBoolean("glow_lyrics", prefs.enableGlowLyrics)
            putFloat("lyrics_size", prefs.lyricsTextSize)
            putInt("skip_seconds", prefs.doubleTapToSkipSeconds)
            putBoolean("mini_swipe", prefs.miniPlayerSwipeToSkip)
            putFloat("anim_speed", prefs.animationSpeedMultiplier)
            putBoolean("scrobbling", prefs.lastFmScrobbling)
            putInt("corner_radius", prefs.cornerRadius)
            putInt("crossfade_duration", prefs.crossfadeDurationMs)
            putString("artwork_quality", prefs.artworkQuality)
            putBoolean("pause_on_unplug", prefs.pauseOnUnplug)
            putBoolean("mark_last_played", prefs.markLastPlayedMedia)
            putStringSet("manage_folders", prefs.manageFolders)
            putString("decoder_priority", prefs.decoderPriority.name)
            putBoolean("require_audio_focus", prefs.requireAudioFocus)
            putBoolean("pause_on_headset", prefs.pauseOnHeadsetDisconnect)
            putBoolean("show_volume_panel", prefs.showSystemVolumePanel)
            putBoolean("volume_boost", prefs.enableVolumeBoost)
            apply()
        }
    }
}

