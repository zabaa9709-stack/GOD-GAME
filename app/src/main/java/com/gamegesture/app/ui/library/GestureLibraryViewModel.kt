package com.gamegesture.app.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamegesture.app.GameGestureApplication
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.service.GestureAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GestureLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GameGestureApplication
    private val gestureRepo = app.gestureRepository

    val gestures: StateFlow<List<GestureSequence>> = gestureRepo.gestures

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun deleteGesture(id: String) {
        viewModelScope.launch {
            gestureRepo.deleteGesture(id)
            _message.value = "Gesture removed"
        }
    }

    fun duplicateGesture(gesture: GestureSequence) {
        viewModelScope.launch {
            val copy = gesture.copy(
                id = UUID.randomUUID().toString(),
                name = "${gesture.name} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            gestureRepo.saveGesture(copy)
            _message.value = "Duplicated: ${copy.name}"
        }
    }

    fun renameGesture(id: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val existing = gestureRepo.getGestureById(id) ?: return@launch
            val updated = existing.copy(name = trimmed, updatedAt = System.currentTimeMillis())
            gestureRepo.saveGesture(updated)
            _message.value = "Renamed to: $trimmed"
        }
    }

    fun testGesture(gesture: GestureSequence) {
        val service = GestureAccessibilityService.instance
        if (service == null) {
            _message.value = "Accessibility Service is offline. Enable it in Settings."
            return
        }
        val settings = app.settingsRepository.settings.value
        service.executeSequence(gesture, triggerHaptic = settings.hapticFeedback) { success, msg ->
            _message.value = if (success) "Executed: ${gesture.name}" else "Dispatch error: $msg"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
