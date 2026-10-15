package com.gamegesture.app.ui.calibration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamegesture.app.GameGestureApplication
import com.gamegesture.app.data.model.CalibrationProfile
import com.gamegesture.app.data.model.CalibrationTarget
import com.gamegesture.app.utils.ScreenMetricsInfo
import com.gamegesture.app.utils.ScreenUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class CalibrationStep {
    SCREEN_DETECTION,
    TARGET_BUTTON,
    ROTATION_ZONE,
    CONFIRMATION
}

data class CalibrationUiState(
    val step: CalibrationStep = CalibrationStep.SCREEN_DETECTION,
    val metrics: ScreenMetricsInfo? = null,
    val targetButtonX: Float = 0.85f,
    val targetButtonY: Float = 0.75f,
    val rotationCenterX: Float = 0.65f,
    val rotationCenterY: Float = 0.55f,
    val rotationRadius: Float = 0.22f,
    val profileName: String = "Calibrated Layout",
    val isSaved: Boolean = false
)

class CalibrationViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as GameGestureApplication
    private val profileRepo = app.profileRepository

    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    init {
        detectScreenMetrics()
    }

    private fun detectScreenMetrics() {
        val context = getApplication<Application>()
        val metrics = ScreenUtils.getScreenMetrics(context)
        _uiState.value = _uiState.value.copy(metrics = metrics)
    }

    fun nextStep() {
        val next = when (_uiState.value.step) {
            CalibrationStep.SCREEN_DETECTION -> CalibrationStep.TARGET_BUTTON
            CalibrationStep.TARGET_BUTTON -> CalibrationStep.ROTATION_ZONE
            CalibrationStep.ROTATION_ZONE -> CalibrationStep.CONFIRMATION
            CalibrationStep.CONFIRMATION -> CalibrationStep.CONFIRMATION
        }
        _uiState.value = _uiState.value.copy(step = next)
    }

    fun previousStep() {
        val prev = when (_uiState.value.step) {
            CalibrationStep.SCREEN_DETECTION -> CalibrationStep.SCREEN_DETECTION
            CalibrationStep.TARGET_BUTTON -> CalibrationStep.SCREEN_DETECTION
            CalibrationStep.ROTATION_ZONE -> CalibrationStep.TARGET_BUTTON
            CalibrationStep.CONFIRMATION -> CalibrationStep.ROTATION_ZONE
        }
        _uiState.value = _uiState.value.copy(step = prev)
    }

    fun setTargetButton(x: Float, y: Float) {
        _uiState.value = _uiState.value.copy(
            targetButtonX = x.coerceIn(0.02f, 0.98f),
            targetButtonY = y.coerceIn(0.02f, 0.98f)
        )
    }

    fun setRotationCenter(cx: Float, cy: Float) {
        _uiState.value = _uiState.value.copy(
            rotationCenterX = cx.coerceIn(0.05f, 0.95f),
            rotationCenterY = cy.coerceIn(0.05f, 0.95f)
        )
    }

    fun setRotationRadius(radius: Float) {
        _uiState.value = _uiState.value.copy(
            rotationRadius = radius.coerceIn(0.05f, 0.45f)
        )
    }

    fun setProfileName(name: String) {
        _uiState.value = _uiState.value.copy(profileName = name)
    }

    fun saveCalibration(onDone: () -> Unit) {
        val state = _uiState.value
        val metrics = state.metrics ?: return

        val calibration = CalibrationProfile(
            id = UUID.randomUUID().toString(),
            profileId = "default",
            profileName = state.profileName.ifBlank { "Screen Calibration" },
            screenWidth = metrics.widthPixels,
            screenHeight = metrics.heightPixels,
            targets = listOf(
                CalibrationTarget(
                    id = "target_btn",
                    label = "Action Trigger",
                    description = "Configured touch location",
                    normalizedX = state.targetButtonX,
                    normalizedY = state.targetButtonY
                )
            ),
            rotationalCenterNormalizedX = state.rotationCenterX,
            rotationalCenterNormalizedY = state.rotationCenterY,
            rotationalRadiusNormalized = state.rotationRadius
        )

        viewModelScope.launch {
            profileRepo.saveCalibration(calibration)
            _uiState.value = _uiState.value.copy(isSaved = true)
            onDone()
        }
    }
}
