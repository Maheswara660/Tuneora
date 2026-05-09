package com.maheswara660.tuneora.feature.settings.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralPreferencesViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<ApplicationPreferences> = preferencesRepository.applicationPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ApplicationPreferences(),
        )

    fun updateFastDelete(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences { it.copy(fastDelete = enabled) }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences { ApplicationPreferences() }
        }
    }
}
