package com.gamegesture.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamegesture.app.GameGestureApplication
import com.gamegesture.app.data.model.AppSettings
import com.gamegesture.app.data.model.DeviceProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val profiles: List<DeviceProfile> = emptyList(),
    val exportedJson: String? = null,
    val message: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GameGestureApplication
    private val settingsRepo = app.settingsRepository
    private val profileRepo = app.profileRepository
    private val gestureRepo = app.gestureRepository

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                settings = settingsRepo.settings.value,
                profiles = profileRepo.profiles.value
            )

            profileRepo.profiles.collect { list ->
                _uiState.value = _uiState.value.copy(profiles = list)
            }
        }
    }

    fun toggleHaptic(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(hapticFeedback = enabled)
        settingsRepo.updateSettings(updated)
        _uiState.value = _uiState.value.copy(settings = updated)
    }

    fun toggleSmoothing(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(gestureSmoothing = enabled)
        settingsRepo.updateSettings(updated)
        _uiState.value = _uiState.value.copy(settings = updated)
    }

    fun toggleDebug(enabled: Boolean) {
        val updated = _uiState.value.settings.copy(debugMode = enabled)
        settingsRepo.updateSettings(updated)
        _uiState.value = _uiState.value.copy(settings = updated)
    }

    fun exportActiveProfile() {
        val activeProfileId = _uiState.value.settings.activeProfileId
        val jsonStr = profileRepo.exportProfilePackage(
            profileId = activeProfileId,
            gestures = gestureRepo.gestures.value
        )
        _uiState.value = _uiState.value.copy(
            exportedJson = jsonStr,
            message = "Profile JSON package exported"
        )
    }

    fun importProfile(jsonStr: String) {
        viewModelScope.launch {
            try {
                val pkg = profileRepo.importProfilePackage(jsonStr)
                // Also save imported gestures
                for (g in pkg.gestures) {
                    gestureRepo.saveGesture(g)
                }
                _uiState.value = _uiState.value.copy(message = "Successfully imported profile: ${pkg.profile.name}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Failed to import profile: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, exportedJson = null)
    }
}
