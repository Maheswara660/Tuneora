package com.maheswara660.tuneora.feature.settings

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import com.maheswara660.tuneora.core.common.model.ThemeConfig
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val uiStateInternal = MutableStateFlow(
        SettingsUiState(
            preferences = preferencesRepository.applicationPreferences.value,
        ),
    )
    val uiState = uiStateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect { preferences ->
                uiStateInternal.update { it.copy(preferences = preferences) }
            }
        }
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.UpdateThemeConfig -> updateThemeConfig(event.themeConfig)
            is SettingsUiEvent.UpdateAccentColorIndex -> updateAccentColorIndex(event.index)
        }
    }

    fun updatePreferences(transform: (ApplicationPreferences) -> ApplicationPreferences) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences(transform)
        }
    }

    private fun updateThemeConfig(themeConfig: ThemeConfig) {
        updatePreferences { it.copy(themeConfig = themeConfig) }
    }

    private fun updateAccentColorIndex(index: Int) {
        updatePreferences { it.copy(accentColorIndex = index) }
    }
}


@Stable
data class SettingsUiState(
    val preferences: ApplicationPreferences = ApplicationPreferences(),
)

sealed interface SettingsUiEvent {
    data class UpdateThemeConfig(val themeConfig: ThemeConfig) : SettingsUiEvent
    data class UpdateAccentColorIndex(val index: Int) : SettingsUiEvent
}
