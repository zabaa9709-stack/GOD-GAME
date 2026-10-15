package com.gamegesture.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamegesture.app.GameGestureApplication
import com.gamegesture.app.data.model.DeviceProfile
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.data.model.ShortcutConfig
import com.gamegesture.app.service.FloatingShortcutService
import com.gamegesture.app.service.GestureAccessibilityService
import com.gamegesture.app.utils.PermissionUtils
import com.gamegesture.app.utils.ScreenUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isAccessibilityEnabled: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val isOverlayActive: Boolean = false,
    val activeProfile: DeviceProfile? = null,
    val screenResolutionText: String = "",
    val gestures: List<GestureSequence> = emptyList(),
    val shortcutConfig: ShortcutConfig? = null,
    val lastExecutedMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GameGestureApplication
    private val gestureRepo = app.gestureRepository
    private val profileRepo = app.profileRepository
    private val settingsRepo = app.settingsRepository

    private val _lastMessage = MutableStateFlow<String?>(null)

    private val serviceState = combine(
        GestureAccessibilityService.isServiceConnected,
        FloatingShortcutService.isOverlayRunning,
        _lastMessage
    ) { connected, overlay, msg ->
        Triple(connected, overlay, msg)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        serviceState,
        gestureRepo.gestures,
        profileRepo.profiles,
        settingsRepo.shortcutConfig
    ) { (serviceConnected, overlayRunning, lastMsg), gestures, profiles, shortcutConfig ->
        val context = getApplication<Application>()
        val hasOverlay = PermissionUtils.hasOverlayPermission(context)
        val metrics = ScreenUtils.getScreenMetrics(context)
        val activeProfile = profiles.find { it.isDefault } ?: profiles.firstOrNull()

        HomeUiState(
            isAccessibilityEnabled = serviceConnected || PermissionUtils.isAccessibilityServiceEnabled(context),
            hasOverlayPermission = hasOverlay,
            isOverlayActive = overlayRunning,
            activeProfile = activeProfile,
            screenResolutionText = "${metrics.widthPixels} × ${metrics.heightPixels} (${metrics.aspectRatio})",
            gestures = gestures,
            shortcutConfig = shortcutConfig,
            lastExecutedMessage = lastMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun refreshPermissions() {
        // StateFlow combine will automatically trigger on access
    }

    fun toggleOverlay() {
        val context = getApplication<Application>()
        if (FloatingShortcutService.isOverlayRunning.value) {
            FloatingShortcutService.stopService(context)
        } else {
            if (PermissionUtils.hasOverlayPermission(context)) {
                FloatingShortcutService.startService(context)
            }
        }
    }

    fun executeGestureQuick(sequence: GestureSequence) {
        val service = GestureAccessibilityService.instance
        if (service == null) {
            _lastMessage.value = "Accessibility Service is not enabled. Enable it in Settings."
            return
        }
        val settings = settingsRepo.settings.value
        service.executeSequence(
            sequence = sequence,
            triggerHaptic = settings.hapticFeedback
        ) { success, msg ->
            _lastMessage.value = if (success) "Executed: ${sequence.name}" else "Failed: $msg"
        }
    }

    fun clearMessage() {
        _lastMessage.value = null
    }
}
