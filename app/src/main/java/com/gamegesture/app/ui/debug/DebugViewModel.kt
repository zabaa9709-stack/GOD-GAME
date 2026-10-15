package com.gamegesture.app.ui.debug

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gamegesture.app.service.GestureAccessibilityService
import com.gamegesture.app.service.GestureExecutionLog
import com.gamegesture.app.utils.PermissionUtils
import com.gamegesture.app.utils.ScreenMetricsInfo
import com.gamegesture.app.utils.ScreenUtils
import kotlinx.coroutines.flow.StateFlow

class DebugViewModel(application: Application) : AndroidViewModel(application) {

    val isServiceConnected: StateFlow<Boolean> = GestureAccessibilityService.isServiceConnected
    val isExecuting: StateFlow<Boolean> = GestureAccessibilityService.isExecuting
    val debugLogs: StateFlow<List<GestureExecutionLog>> = GestureAccessibilityService.debugLogs

    fun getScreenMetrics(): ScreenMetricsInfo {
        return ScreenUtils.getScreenMetrics(getApplication())
    }

    fun hasOverlay(): Boolean {
        return PermissionUtils.hasOverlayPermission(getApplication())
    }

    fun clearLogs() {
        GestureAccessibilityService.clearDebugLogs()
    }
}
