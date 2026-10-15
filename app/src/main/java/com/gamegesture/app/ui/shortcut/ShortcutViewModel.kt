package com.gamegesture.app.ui.shortcut

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamegesture.app.GameGestureApplication
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.data.model.ShortcutConfig
import com.gamegesture.app.service.FloatingShortcutService
import com.gamegesture.app.utils.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ShortcutUiState(
    val config: ShortcutConfig = ShortcutConfig(gestureSequenceId = "preset_360_movement"),
    val allGestures: List<GestureSequence> = emptyList(),
    val isOverlayActive: Boolean = false,
    val hasPermission: Boolean = false,
    val message: String? = null
)

class ShortcutViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GameGestureApplication
    private val settingsRepo = app.settingsRepository
    private val gestureRepo = app.gestureRepository

    private val _uiState = MutableStateFlow(ShortcutUiState())
    val uiState: StateFlow<ShortcutUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val initialConfig = settingsRepo.shortcutConfig.value
            val initialGestures = gestureRepo.gestures.value
            val context = getApplication<Application>()
            _uiState.value = ShortcutUiState(
                config = initialConfig,
                allGestures = initialGestures,
                isOverlayActive = FloatingShortcutService.isOverlayRunning.value,
                hasPermission = PermissionUtils.hasOverlayPermission(context)
            )

            // Collect changes
            gestureRepo.gestures.collect { list ->
                _uiState.value = _uiState.value.copy(allGestures = list)
            }
        }
    }

    fun updateAssignedGesture(gestureId: String) {
        val updated = _uiState.value.config.copy(gestureSequenceId = gestureId)
        _uiState.value = _uiState.value.copy(config = updated)
    }

    fun updateSize(sizeDp: Int) {
        val updated = _uiState.value.config.copy(sizeDp = sizeDp)
        _uiState.value = _uiState.value.copy(config = updated)
    }

    fun updateOpacity(opacity: Float) {
        val updated = _uiState.value.config.copy(opacity = opacity)
        _uiState.value = _uiState.value.copy(config = updated)
    }

    fun updateColor(hex: String) {
        val updated = _uiState.value.config.copy(colorHex = hex)
        _uiState.value = _uiState.value.copy(config = updated)
    }

    fun toggleLock(locked: Boolean) {
        val updated = _uiState.value.config.copy(isLocked = locked)
        _uiState.value = _uiState.value.copy(config = updated)
    }

    fun saveConfig() {
        val cfg = _uiState.value.config
        settingsRepo.updateShortcutConfig(cfg)
        _uiState.value = _uiState.value.copy(message = "Shortcut configuration saved")
    }

    fun toggleOverlay() {
        val context = getApplication<Application>()
        if (FloatingShortcutService.isOverlayRunning.value) {
            FloatingShortcutService.stopService(context)
            _uiState.value = _uiState.value.copy(isOverlayActive = false)
        } else {
            if (PermissionUtils.hasOverlayPermission(context)) {
                saveConfig()
                FloatingShortcutService.startService(context)
                _uiState.value = _uiState.value.copy(isOverlayActive = true)
            } else {
                _uiState.value = _uiState.value.copy(message = "Overlay permission required")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
