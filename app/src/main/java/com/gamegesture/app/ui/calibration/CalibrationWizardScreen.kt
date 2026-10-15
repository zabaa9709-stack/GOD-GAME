package com.gamegesture.app.ui.calibration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamegesture.app.ui.components.GamingCard
import com.gamegesture.app.ui.components.GamingHeader
import com.gamegesture.app.ui.components.NeonButton
import com.gamegesture.app.ui.components.NeonOutlinedButton
import com.gamegesture.app.ui.theme.CyberCardBg
import com.gamegesture.app.ui.theme.CyberCardBorder
import com.gamegesture.app.ui.theme.CyberDarkBg
import com.gamegesture.app.ui.theme.CyberSurface
import com.gamegesture.app.ui.theme.NeonCyan
import com.gamegesture.app.ui.theme.NeonEmerald
import com.gamegesture.app.ui.theme.NeonPurple
import com.gamegesture.app.ui.theme.TextMuted
import com.gamegesture.app.ui.theme.TextPrimary
import com.gamegesture.app.ui.theme.TextSecondary

@Composable
fun CalibrationWizardScreen(
    onBack: () -> Unit,
    viewModel: CalibrationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CyberDarkBg,
        topBar = {
            GamingHeader(
                title = "DEVICE CALIBRATION",
                subtitle = "Adapt gestures to your device screen aspect ratio",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step Progress Indicator
            StepProgressBar(currentStep = state.step)

            when (state.step) {
                CalibrationStep.SCREEN_DETECTION -> {
                    ScreenDetectionStep(
                        metrics = state.metrics,
                        onNext = { viewModel.nextStep() }
                    )
                }

                CalibrationStep.TARGET_BUTTON -> {
                    TargetButtonStep(
                        buttonX = state.targetButtonX,
                        buttonY = state.targetButtonY,
                        onPointSelected = { x, y -> viewModel.setTargetButton(x, y) },
                        onNext = { viewModel.nextStep() },
                        onBack = { viewModel.previousStep() }
                    )
                }

                CalibrationStep.ROTATION_ZONE -> {
                    RotationZoneStep(
                        centerX = state.rotationCenterX,
                        centerY = state.rotationCenterY,
                        radius = state.rotationRadius,
                        onCenterSelected = { cx, cy -> viewModel.setRotationCenter(cx, cy) },
                        onRadiusChanged = { r -> viewModel.setRotationRadius(r) },
                        onNext = { viewModel.nextStep() },
                        onBack = { viewModel.previousStep() }
                    )
                }

                CalibrationStep.CONFIRMATION -> {
                    ConfirmationStep(
                        profileName = state.profileName,
                        targetX = state.targetButtonX,
                        targetY = state.targetButtonY,
                        rotCenterX = state.rotationCenterX,
                        rotCenterY = state.rotationCenterY,
                        radius = state.rotationRadius,
                        onNameChanged = { viewModel.setProfileName(it) },
                        onSave = { viewModel.saveCalibration(onBack) },
                        onBack = { viewModel.previousStep() }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepProgressBar(currentStep: CalibrationStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepDot(num = "1", label = "Screen", isCurrent = currentStep == CalibrationStep.SCREEN_DETECTION, isDone = currentStep.ordinal > 0)
        StepLine(isDone = currentStep.ordinal > 0, modifier = Modifier.weight(1f))
        StepDot(num = "2", label = "Target", isCurrent = currentStep == CalibrationStep.TARGET_BUTTON, isDone = currentStep.ordinal > 1)
        StepLine(isDone = currentStep.ordinal > 1, modifier = Modifier.weight(1f))
        StepDot(num = "3", label = "Swivel", isCurrent = currentStep == CalibrationStep.ROTATION_ZONE, isDone = currentStep.ordinal > 2)
        StepLine(isDone = currentStep.ordinal > 2, modifier = Modifier.weight(1f))
        StepDot(num = "4", label = "Save", isCurrent = currentStep == CalibrationStep.CONFIRMATION, isDone = false)
    }
}

@Composable
private fun StepDot(num: String, label: String, isCurrent: Boolean, isDone: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCurrent -> NeonCyan
                        isDone -> NeonEmerald
                        else -> CyberSurface
                    }
                )
                .border(1.dp, if (isCurrent) NeonCyan else CyberCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDone) "✓" else num,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent || isDone) CyberDarkBg else TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = if (isCurrent) NeonCyan else TextMuted)
    }
}

@Composable
private fun StepLine(isDone: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 12.dp)
            .height(2.dp)
            .background(if (isDone) NeonEmerald else CyberCardBorder)
    )
}

@Composable
private fun ScreenDetectionStep(
    metrics: com.gamegesture.app.utils.ScreenMetricsInfo?,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "STEP 1: SCREEN SPECS DETECTED",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Text(
                text = "GameGesture will normalize all touch coordinates between 0.00 and 1.00 so your shortcuts translate across resolutions seamlessly.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            if (metrics != null) {
                GamingCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricRow("Display Resolution", "${metrics.widthPixels} × ${metrics.heightPixels} px")
                        MetricRow("Aspect Ratio", metrics.aspectRatio)
                        MetricRow("Screen Density", "${metrics.densityDpi} DPI (${metrics.density}x)")
                        MetricRow("Orientation", if (metrics.isLandscape) "Landscape" else "Portrait")
                    }
                }
            }
        }

        NeonButton(
            text = "CONTINUE TO TARGET CALIBRATION",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TargetButtonStep(
    buttonX: Float,
    buttonY: Float,
    onPointSelected: (Float, Float) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "STEP 2: MARK TARGET ACTION BUTTON",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Text(
                text = "Tap on the canvas below at the approximate position of your game's utility or fire button.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF07090D))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val normX = (offset.x / size.width).coerceIn(0f, 1f)
                            val normY = (offset.y / size.height).coerceIn(0f, 1f)
                            onPointSelected(normX, normY)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cw = size.width
                    val ch = size.height
                    val px = buttonX * cw
                    val py = buttonY * ch

                    // Target crosshair
                    drawLine(Color(0x3300E5FF), Offset(px, 0f), Offset(px, ch), strokeWidth = 1f)
                    drawLine(Color(0x3300E5FF), Offset(0f, py), Offset(cw, py), strokeWidth = 1f)

                    drawCircle(NeonCyan.copy(alpha = 0.25f), radius = 30f, center = Offset(px, py))
                    drawCircle(NeonCyan, radius = 10f, center = Offset(px, py))
                }

                Text(
                    text = "Normalized: (${(buttonX * 100).toInt()}%, ${(buttonY * 100).toInt()}%)",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    modifier = Modifier.padding(10.dp).align(Alignment.TopEnd)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeonOutlinedButton(text = "BACK", onClick = onBack, modifier = Modifier.weight(0.8f))
            NeonButton(text = "CONFIRM TARGET", onClick = onNext, modifier = Modifier.weight(1.2f))
        }
    }
}

@Composable
private fun RotationZoneStep(
    centerX: Float,
    centerY: Float,
    radius: Float,
    onCenterSelected: (Float, Float) -> Unit,
    onRadiusChanged: (Float) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "STEP 3: 360° ROTATION / AIM ZONE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            Text(
                text = "Tap to set center of rotation, then adjust radius to match your game's camera touch area.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF07090D))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val normX = (offset.x / size.width).coerceIn(0f, 1f)
                            val normY = (offset.y / size.height).coerceIn(0f, 1f)
                            onCenterSelected(normX, normY)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cw = size.width
                    val ch = size.height
                    val cx = centerX * cw
                    val cy = centerY * ch
                    val r = radius * cw.coerceAtMost(ch)

                    // Draw rotation boundary
                    drawCircle(
                        color = NeonPurple,
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 4f)
                    )
                    drawCircle(NeonPurple.copy(alpha = 0.15f), radius = r, center = Offset(cx, cy))
                    drawCircle(NeonCyan, radius = 8f, center = Offset(cx, cy))
                }
            }

            Column {
                Text(
                    text = "Swivel Radius: ${(radius * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Slider(
                    value = radius,
                    onValueChange = onRadiusChanged,
                    valueRange = 0.08f..0.45f,
                    colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeonOutlinedButton(text = "BACK", onClick = onBack, modifier = Modifier.weight(0.8f))
            NeonButton(text = "CONFIRM ROTATION", onClick = onNext, color = NeonPurple, modifier = Modifier.weight(1.2f))
        }
    }
}

@Composable
private fun ConfirmationStep(
    profileName: String,
    targetX: Float,
    targetY: Float,
    rotCenterX: Float,
    rotCenterY: Float,
    radius: Float,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "STEP 4: SAVE CALIBRATION PROFILE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonEmerald
            )

            OutlinedTextField(
                value = profileName,
                onValueChange = onNameChanged,
                label = { Text("Profile Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonEmerald,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedLabelColor = NeonEmerald
                )
            )

            GamingCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CALIBRATION DATA",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    MetricRow("Target Button (X, Y)", "${(targetX * 100).toInt()}%, ${(targetY * 100).toInt()}%")
                    MetricRow("Rotation Center (X, Y)", "${(rotCenterX * 100).toInt()}%, ${(rotCenterY * 100).toInt()}%")
                    MetricRow("Rotation Radius", "${(radius * 100).toInt()}%")
                    MetricRow("Coordinate Space", "Normalized [0.00 .. 1.00]")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeonOutlinedButton(text = "BACK", onClick = onBack, modifier = Modifier.weight(0.8f))
            NeonButton(
                text = "SAVE PROFILE",
                icon = Icons.Default.Check,
                onClick = onSave,
                color = NeonEmerald,
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
