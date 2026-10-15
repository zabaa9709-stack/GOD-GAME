package com.gamegesture.app.ui.recorder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamegesture.app.GameGestureApplication
import com.gamegesture.app.gesture.GestureRecorder
import com.gamegesture.app.gesture.RecorderSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GestureRecorderViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GameGestureApplication
    private val gestureRepo = app.gestureRepository

    val recorder = GestureRecorder()
    val snapshot: StateFlow<RecorderSnapshot> = recorder.snapshot

    private val _gestureName = MutableStateFlow("Recorded Gesture")
    val gestureName: StateFlow<String> = _gestureName.asStateFlow()

    private val _applySmoothing = MutableStateFlow(true)
    val applySmoothing: StateFlow<Boolean> = _applySmoothing.asStateFlow()

    fun updateName(name: String) {
        _gestureName.value = name
    }

    fun toggleSmoothing() {
        _applySmoothing.value = !_applySmoothing.value
    }

    fun saveRecordedGesture(onSaved: () -> Unit) {
        val sequence = recorder.toGestureSequence(
            name = _gestureName.value.ifBlank { "Recorded Gesture" },
            applySmoothing = _applySmoothing.value
        )

        viewModelScope.launch {
            gestureRepo.saveGesture(sequence)
            recorder.reset()
            onSaved()
        }
    }

    fun reset() {
        recorder.reset()
    }
}
