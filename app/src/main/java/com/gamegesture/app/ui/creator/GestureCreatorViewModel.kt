package com.gamegesture.app.ui.creator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamegesture.app.GameGestureApplication
import com.gamegesture.app.data.model.ActionType
import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.GestureSequence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class CreatorUiState(
    val gestureId: String = UUID.randomUUID().toString(),
    val name: String = "360° Rotational Camera",
    val description: String = "Full circular swivel movement",
    val actions: List<GestureAction> = listOf(
        GestureAction.CircularAction(
            centerX = 0.65f,
            centerY = 0.55f,
            radius = 0.22f,
            startAngleDeg = 0f,
            sweepAngleDeg = 360f,
            isClockwise = true,
            durationMs = 280L,
            pointCount = 36
        )
    ),
    val repeatCount: Int = 1,
    val delayBetweenRepeatsMs: Long = 50L,
    val isEditing: Boolean = false,
    val isSaved: Boolean = false
)

class GestureCreatorViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GameGestureApplication
    private val gestureRepo = app.gestureRepository

    private val _uiState = MutableStateFlow(CreatorUiState())
    val uiState: StateFlow<CreatorUiState> = _uiState.asStateFlow()

    fun initForGesture(gestureId: String?) {
        if (gestureId.isNullOrEmpty()) {
            _uiState.value = CreatorUiState()
            return
        }
        val existing = gestureRepo.getGestureById(gestureId)
        if (existing != null) {
            _uiState.value = CreatorUiState(
                gestureId = existing.id,
                name = existing.name,
                description = existing.description,
                actions = existing.actions,
                repeatCount = existing.repeatCount,
                delayBetweenRepeatsMs = existing.delayBetweenRepeatsMs,
                isEditing = true
            )
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateDescription(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun updateRepeatCount(count: Int) {
        _uiState.value = _uiState.value.copy(repeatCount = count.coerceIn(1, 10))
    }

    fun addAction(action: GestureAction) {
        val current = _uiState.value.actions.toMutableList()
        current.add(action)
        _uiState.value = _uiState.value.copy(actions = current)
    }

    fun removeAction(index: Int) {
        val current = _uiState.value.actions.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(actions = current)
        }
    }

    fun updateAction(index: Int, action: GestureAction) {
        val current = _uiState.value.actions.toMutableList()
        if (index in current.indices) {
            current[index] = action
            _uiState.value = _uiState.value.copy(actions = current)
        }
    }

    fun saveGesture(onDone: () -> Unit) {
        val state = _uiState.value
        val sequence = GestureSequence(
            id = state.gestureId,
            name = state.name.ifBlank { "Custom Gesture" },
            description = state.description,
            actions = state.actions,
            repeatCount = state.repeatCount,
            delayBetweenRepeatsMs = state.delayBetweenRepeatsMs,
            isCalibrated = true,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            gestureRepo.saveGesture(sequence)
            _uiState.value = _uiState.value.copy(isSaved = true)
            onDone()
        }
    }

    fun buildCurrentSequence(): GestureSequence {
        val state = _uiState.value
        return GestureSequence(
            id = state.gestureId,
            name = state.name,
            description = state.description,
            actions = state.actions,
            repeatCount = state.repeatCount
        )
    }
}
