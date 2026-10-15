package com.gamegesture.app.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DeviceProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val aspectRatio: String = "20:9",
    val densityDpi: Int = 420,
    val isDefault: Boolean = false,
    val description: String = ""
)

@Serializable
data class CalibrationTarget(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val description: String,
    val normalizedX: Float,
    val normalizedY: Float
)

@Serializable
data class CalibrationProfile(
    val id: String = UUID.randomUUID().toString(),
    val profileId: String = "default",
    val profileName: String = "Default Profile",
    val screenWidth: Int = 1080,
    val screenHeight: Int = 2400,
    val targets: List<CalibrationTarget> = emptyList(),
    val rotationalCenterNormalizedX: Float = 0.5f,
    val rotationalCenterNormalizedY: Float = 0.5f,
    val rotationalRadiusNormalized: Float = 0.25f,
    val calibratedAt: Long = System.currentTimeMillis()
)

@Serializable
data class ShortcutConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Shortcut #1",
    val gestureSequenceId: String,
    val normalizedPositionX: Float = 0.88f,
    val normalizedPositionY: Float = 0.35f,
    val sizeDp: Int = 54,
    val opacity: Float = 0.85f,
    val colorHex: String = "#00E5FF",
    val iconType: String = "crosshair",
    val isLocked: Boolean = false,
    val isEnabled: Boolean = true
)

@Serializable
data class AppSettings(
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val defaultDurationMs: Long = 300L,
    val gestureSmoothing: Boolean = true,
    val pointDensity: Int = 36,
    val debugMode: Boolean = true,
    val activeProfileId: String = "default",
    val isOverlayEnabled: Boolean = false
)

@Serializable
data class ExportedProfilePackage(
    val profile: DeviceProfile,
    val calibration: CalibrationProfile?,
    val gestures: List<GestureSequence>,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0"
)
