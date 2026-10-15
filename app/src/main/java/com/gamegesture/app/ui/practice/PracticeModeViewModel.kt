package com.gamegesture.app.ui.practice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamegesture.app.GameGestureApplication
import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.service.GestureAccessibilityService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PracticeUiState(
    val selectedGesture: GestureSequence? = null,
    val allGestures: List<GestureSequence> = emptyList(),
    val playbackSpeed: Float = 1.0f,
    val isSimulating: Boolean = false,
    val simulationStepText: String? = null,
    val currentStepIndex: Int = -1,
    val isExecutingOnDevice: Boolean = false,
    val executionResult: String? = null
)

class PracticeModeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GameGestureApplication
    private val gestureRepo = app.gestureRepository

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null

    init {
        viewModelScope.launch {
            gestureRepo.gestures.collect { list ->
                val current = _uiState.value.selectedGesture
                val updatedSelected = if (current != null) {
                    list.find { it.id == current.id } ?: list.firstOrNull()
                } else {
                    list.firstOrNull()
                }
                _uiState.value = _uiState.value.copy(
                    allGestures = list,
                    selectedGesture = updatedSelected
                )
            }
        }
    }

    fun selectGestureById(id: String?) {
        if (id.isNullOrEmpty()) return
        val found = gestureRepo.getGestureById(id)
        if (found != null) {
            cancelSimulation()
            _uiState.value = _uiState.value.copy(
                selectedGesture = found,
                simulationStepText = null,
                currentStepIndex = -1
            )
        }
    }

    fun setSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun replaySimulation() {
        val gesture = _uiState.value.selectedGesture ?: return
        cancelSimulation()

        simulationJob = viewModelScope.launch {
            val speed = _uiState.value.playbackSpeed.coerceAtLeast(0.2f)
            _uiState.value = _uiState.value.copy(
                isSimulating = true,
                simulationStepText = "Simulating: ${gesture.name} (Speed: ${speed}x)",
                currentStepIndex = 0
            )

            for (repeat in 1..gesture.repeatCount) {
                if (gesture.repeatCount > 1) {
                    _uiState.value = _uiState.value.copy(
                        simulationStepText = "Cycle $repeat of ${gesture.repeatCount}..."
                    )
                }

                for (i in gesture.actions.indices) {
                    val action = gesture.actions[i]
                    val desc = when (action) {
                        is GestureAction.TapAction -> "Tap at (${(action.x * 100).toInt()}%, ${(action.y * 100).toInt()}%)"
                        is GestureAction.SwipeAction -> "Swipe path (${action.durationMs}ms)"
                        is GestureAction.CircularAction -> "360° Circular Sweep (${action.durationMs}ms)"
                        is GestureAction.FreeformAction -> "Freeform Motion (${action.points.size} pts, ${action.durationMs}ms)"
                        is GestureAction.LongPressAction -> "Long Press (${action.durationMs}ms)"
                        is GestureAction.DelayAction -> "Delay Pause (${action.delayMs}ms)"
                    }
                    _uiState.value = _uiState.value.copy(
                        currentStepIndex = i,
                        simulationStepText = "Step ${i + 1}/${gesture.actions.size}: $desc"
                    )

                    val actionDuration = when (action) {
                        is GestureAction.TapAction -> 150L
                        is GestureAction.SwipeAction -> action.durationMs
                        is GestureAction.CircularAction -> action.durationMs
                        is GestureAction.FreeformAction -> action.durationMs
                        is GestureAction.LongPressAction -> action.durationMs
                        is GestureAction.DelayAction -> action.delayMs
                    }
                    delay(((action.delayMs + actionDuration) / speed).toLong().coerceAtLeast(80L))
                }

                if (repeat < gesture.repeatCount && gesture.delayBetweenRepeatsMs > 0) {
                    delay((gesture.delayBetweenRepeatsMs / speed).toLong())
                }
            }

            _uiState.value = _uiState.value.copy(
                isSimulating = false,
                currentStepIndex = -1,
                simulationStepText = "Replay Complete: All ${gesture.actions.size} step(s) simulated cleanly ✓"
            )
        }
    }

    fun cancelSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        if (_uiState.value.isSimulating) {
            _uiState.value = _uiState.value.copy(isSimulating = false, currentStepIndex = -1)
        }
    }

    fun executeRealGesture() {
        val gesture = _uiState.value.selectedGesture ?: return
        val service = GestureAccessibilityService.instance
        if (service == null) {
            _uiState.value = _uiState.value.copy(
                executionResult = "Accessibility Service is not enabled. Turn it on in Android Settings."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isExecutingOnDevice = true, executionResult = null)
        service.executeSequence(gesture) { success, msg ->
            _uiState.value = _uiState.value.copy(
                isExecutingOnDevice = false,
                executionResult = if (success) "Dispatched successfully: ${gesture.name}" else "Dispatch failed: $msg"
            )
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(executionResult = null)
    }

    override fun onCleared() {
        super.onCleared()
        cancelSimulation()
    }
}
