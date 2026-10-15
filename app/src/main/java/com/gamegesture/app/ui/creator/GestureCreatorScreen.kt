package com.gamegesture.app.ui.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamegesture.app.data.model.ActionType
import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.ui.components.GamingCard
import com.gamegesture.app.ui.components.GamingHeader
import com.gamegesture.app.ui.components.GesturePreviewCanvas
import com.gamegesture.app.ui.components.NeonButton
import com.gamegesture.app.ui.theme.CyberCardBg
import com.gamegesture.app.ui.theme.CyberCardBorder
import com.gamegesture.app.ui.theme.CyberDarkBg
import com.gamegesture.app.ui.theme.CyberSurface
import com.gamegesture.app.ui.theme.NeonCyan
import com.gamegesture.app.ui.theme.NeonEmerald
import com.gamegesture.app.ui.theme.NeonPurple
import com.gamegesture.app.ui.theme.NeonRed
import com.gamegesture.app.ui.theme.TextMuted
import com.gamegesture.app.ui.theme.TextPrimary
import com.gamegesture.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureCreatorScreen(
    gestureId: String?,
    onBack: () -> Unit,
    viewModel: GestureCreatorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(gestureId) {
        viewModel.initForGesture(gestureId)
    }

    var selectedActionType by remember { mutableStateOf(ActionType.CIRCULAR) }

    // Circular action parameters state
    var circleCenterX by remember { mutableFloatStateOf(0.65f) }
    var circleCenterY by remember { mutableFloatStateOf(0.55f) }
    var circleRadius by remember { mutableFloatStateOf(0.22f) }
    var circleDuration by remember { mutableFloatStateOf(280f) }
    var circlePoints by remember { mutableIntStateOf(36) }
    var isClockwise by remember { mutableStateOf(true) }

    // Swipe parameters state
    var swipeStartX by remember { mutableFloatStateOf(0.80f) }
    var swipeStartY by remember { mutableFloatStateOf(0.50f) }
    var swipeEndX by remember { mutableFloatStateOf(0.30f) }
    var swipeEndY by remember { mutableFloatStateOf(0.50f) }
    var swipeDuration by remember { mutableFloatStateOf(160f) }

    // Tap parameters state
    var tapX by remember { mutableFloatStateOf(0.80f) }
    var tapY by remember { mutableFloatStateOf(0.60f) }

    // Delay parameters state
    var delayMs by remember { mutableFloatStateOf(60f) }

    Scaffold(
        containerColor = CyberDarkBg,
        topBar = {
            GamingHeader(
                title = if (state.isEditing) "EDIT GESTURE" else "CREATE GESTURE",
                subtitle = "Configure normalized touch action sequence",
                onBack = onBack,
                actions = {
                    NeonButton(
                        text = "SAVE",
                        onClick = { viewModel.saveGesture(onBack) },
                        icon = Icons.Default.Check,
                        color = NeonEmerald
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Interactive Animated Gesture Preview Canvas
            item {
                Column {
                    Text(
                        text = "LIVE MOTION PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GesturePreviewCanvas(
                        sequence = viewModel.buildCurrentSequence(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        animate = true
                    )
                }
            }

            // 2. Gesture details: Name & description
            item {
                GamingCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("Gesture Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedLabelColor = NeonCyan
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            label = { Text("Description / Usage Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedLabelColor = NeonCyan
                            )
                        )

                        // Repeat Count Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Repeat Execution",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.updateRepeatCount(state.repeatCount - 1) },
                                    modifier = Modifier.size(34.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyberDarkBg,
                                        contentColor = NeonCyan
                                    )
                                ) {
                                    Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "${state.repeatCount}x",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { viewModel.updateRepeatCount(state.repeatCount + 1) },
                                    modifier = Modifier.size(34.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyberDarkBg,
                                        contentColor = NeonCyan
                                    )
                                ) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Current Action Steps in Sequence
            item {
                Text(
                    text = "ACTION SEQUENCE (${state.actions.size} STEPS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            itemsIndexed(state.actions) { index, action ->
                ActionStepItem(
                    index = index + 1,
                    action = action,
                    onDelete = { viewModel.removeAction(index) }
                )
            }

            // 4. Add Step Configurator
            item {
                GamingCard(borderColor = NeonPurple.copy(alpha = 0.5f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "+ ADD NEW ACTION STEP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )

                        // Action type selector tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ActionTypeTab(
                                label = "360° CIRCLE",
                                isSelected = selectedActionType == ActionType.CIRCULAR,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedActionType = ActionType.CIRCULAR }
                            )
                            ActionTypeTab(
                                label = "SWIPE",
                                isSelected = selectedActionType == ActionType.SWIPE,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedActionType = ActionType.SWIPE }
                            )
                            ActionTypeTab(
                                label = "TAP",
                                isSelected = selectedActionType == ActionType.TAP,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedActionType = ActionType.TAP }
                            )
                            ActionTypeTab(
                                label = "DELAY",
                                isSelected = selectedActionType == ActionType.DELAY,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedActionType = ActionType.DELAY }
                            )
                        }

                        // Config controls depending on type
                        when (selectedActionType) {
                            ActionType.CIRCULAR -> {
                                SliderField(
                                    label = "Center X: ${(circleCenterX * 100).toInt()}%",
                                    value = circleCenterX,
                                    onValueChange = { circleCenterX = it },
                                    range = 0.1f..0.9f
                                )
                                SliderField(
                                    label = "Center Y: ${(circleCenterY * 100).toInt()}%",
                                    value = circleCenterY,
                                    onValueChange = { circleCenterY = it },
                                    range = 0.1f..0.9f
                                )
                                SliderField(
                                    label = "Radius: ${(circleRadius * 100).toInt()}%",
                                    value = circleRadius,
                                    onValueChange = { circleRadius = it },
                                    range = 0.05f..0.45f
                                )
                                SliderField(
                                    label = "Duration: ${circleDuration.toInt()} ms",
                                    value = circleDuration,
                                    onValueChange = { circleDuration = it },
                                    range = 80f..1000f
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isClockwise) "Clockwise ↻" else "Counter-Clockwise ↺",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Switch(
                                        checked = isClockwise,
                                        onCheckedChange = { isClockwise = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                                    )
                                }
                            }

                            ActionType.SWIPE -> {
                                SliderField(
                                    label = "Start: (${(swipeStartX * 100).toInt()}%, ${(swipeStartY * 100).toInt()}%)",
                                    value = swipeStartX,
                                    onValueChange = { swipeStartX = it },
                                    range = 0.05f..0.95f
                                )
                                SliderField(
                                    label = "End: (${(swipeEndX * 100).toInt()}%, ${(swipeEndY * 100).toInt()}%)",
                                    value = swipeEndX,
                                    onValueChange = { swipeEndX = it },
                                    range = 0.05f..0.95f
                                )
                                SliderField(
                                    label = "Duration: ${swipeDuration.toInt()} ms",
                                    value = swipeDuration,
                                    onValueChange = { swipeDuration = it },
                                    range = 50f..1000f
                                )
                            }

                            ActionType.TAP -> {
                                SliderField(
                                    label = "Tap Position X: ${(tapX * 100).toInt()}%",
                                    value = tapX,
                                    onValueChange = { tapX = it },
                                    range = 0.05f..0.95f
                                )
                                SliderField(
                                    label = "Tap Position Y: ${(tapY * 100).toInt()}%",
                                    value = tapY,
                                    onValueChange = { tapY = it },
                                    range = 0.05f..0.95f
                                )
                            }

                            ActionType.DELAY -> {
                                SliderField(
                                    label = "Pause Delay: ${delayMs.toInt()} ms",
                                    value = delayMs,
                                    onValueChange = { delayMs = it },
                                    range = 10f..1000f
                                )
                            }

                            else -> {}
                        }

                        NeonButton(
                            text = "APPEND ACTION TO SEQUENCE",
                            icon = Icons.Default.Add,
                            color = NeonPurple,
                            onClick = {
                                val actionToAdd: GestureAction = when (selectedActionType) {
                                    ActionType.CIRCULAR -> GestureAction.CircularAction(
                                        centerX = circleCenterX,
                                        centerY = circleCenterY,
                                        radius = circleRadius,
                                        durationMs = circleDuration.toLong(),
                                        pointCount = circlePoints,
                                        isClockwise = isClockwise
                                    )
                                    ActionType.SWIPE -> GestureAction.SwipeAction(
                                        startX = swipeStartX,
                                        startY = swipeStartY,
                                        endX = swipeEndX,
                                        endY = swipeEndY,
                                        durationMs = swipeDuration.toLong()
                                    )
                                    ActionType.TAP -> GestureAction.TapAction(
                                        x = tapX,
                                        y = tapY
                                    )
                                    ActionType.DELAY -> GestureAction.DelayAction(
                                        delayMs = delayMs.toLong()
                                    )
                                    else -> GestureAction.TapAction(0.5f, 0.5f)
                                }
                                viewModel.addAction(actionToAdd)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ActionTypeTab(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonPurple else CyberSurface)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) CyberDarkBg else TextSecondary
        )
    }
}

@Composable
private fun SliderField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Column {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan
            )
        )
    }
}

@Composable
private fun ActionStepItem(
    index: Int,
    action: GestureAction,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "$index", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    val title = when (action) {
                        is GestureAction.TapAction -> "TAP at (${(action.x * 100).toInt()}%, ${(action.y * 100).toInt()}%)"
                        is GestureAction.SwipeAction -> "SWIPE (${action.durationMs}ms)"
                        is GestureAction.CircularAction -> "360° CIRCLE (${action.durationMs}ms, r=${(action.radius * 100).toInt()}%)"
                        is GestureAction.FreeformAction -> "FREEFORM PATH (${action.points.size} pts, ${action.durationMs}ms)"
                        is GestureAction.DelayAction -> "DELAY (${action.delayMs}ms)"
                        is GestureAction.LongPressAction -> "LONG PRESS (${action.durationMs}ms)"
                    }
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    if (action.delayMs > 0 && action !is GestureAction.DelayAction) {
                        Text(text = "Pre-delay: ${action.delayMs}ms", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Step",
                    tint = NeonRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
