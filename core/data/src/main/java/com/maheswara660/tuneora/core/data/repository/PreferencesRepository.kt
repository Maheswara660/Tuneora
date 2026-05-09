package com.maheswara660.tuneora.core.data.repository

import com.maheswara660.tuneora.core.common.model.ApplicationPreferences
import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    val applicationPreferences: StateFlow<ApplicationPreferences>
    suspend fun updateApplicationPreferences(transform: (ApplicationPreferences) -> ApplicationPreferences)
}
