package com.gamegesture.app.ui.recorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamegesture.app.gesture.RecorderStatus
import com.gamegesture.app.ui.components.GamingCard
import com.gamegesture.app.ui.components.GamingHeader
import com.gamegesture.app.ui.components.NeonButton
import com.gamegesture.app.ui.components.NeonOutlinedButton
import com.gamegesture.app.ui.components.StatusBadge
import com.gamegesture.app.ui.theme.CyberCardBg
import com.gamegesture.app.ui.theme.CyberCardBorder
import com.gamegesture.app.ui.theme.CyberDarkBg
import com.gamegesture.app.ui.theme.NeonCyan
import com.gamegesture.app.ui.theme.NeonEmerald
import com.gamegesture.app.ui.theme.NeonPurple
import com.gamegesture.app.ui.theme.NeonRed
import com.gamegesture.app.ui.theme.TextMuted
import com.gamegesture.app.ui.theme.TextPrimary
import com.gamegesture.app.ui.theme.TextSecondary

@Composable
fun GestureRecorderScreen(
    onBack: () -> Unit,
    viewModel: GestureRecorderViewModel = viewModel()
) {
    val snapshot by viewModel.snapshot.collectAsState()
    val gestureName by viewModel.gestureName.collectAsState()
    val applySmoothing by viewModel.applySmoothing.collectAsState()

    Scaffold(
        containerColor = CyberDarkBg,
        topBar = {
            GamingHeader(
                title = "GESTURE RECORDER",
                subtitle = "Touch & drag on canvas to capture raw path",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. HUD Stats bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HudMetric(label = "DURATION", value = "${snapshot.elapsedDurationMs} ms")
                    HudMetric(label = "POINTS", value = "${snapshot.pointCount}")
                }
                StatusBadge(
                    label = when (snapshot.status) {
                        RecorderStatus.IDLE -> "TOUCH TO RECORD"
                        RecorderStatus.RECORDING -> "RECORDING ●"
                        RecorderStatus.COMPLETED -> "CAPTURED ✓"
                        RecorderStatus.PAUSED -> "PAUSED"
                    },
                    isActive = snapshot.status == RecorderStatus.RECORDING,
                    activeColor = NeonRed,
                    inactiveColor = if (snapshot.status == RecorderStatus.COMPLETED) NeonEmerald else TextMuted
                )
            }

            // 2. Interactive full-area recording canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF07090D))
                    .border(
                        1.dp,
                        if (snapshot.status == RecorderStatus.RECORDING) NeonRed else CyberCardBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val normX = (offset.x / size.width).coerceIn(0f, 1f)
                                val normY = (offset.y / size.height).coerceIn(0f, 1f)
                                viewModel.recorder.onTouchDown(normX, normY)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                                viewModel.recorder.onTouchMove(normX, normY)
                            },
                            onDragEnd = {
                                val last = snapshot.points.lastOrNull()
                                if (last != null) {
                                    viewModel.recorder.onTouchUp(last.x, last.y)
                                } else {
                                    viewModel.recorder.stopRecording()
                                }
                            },
                            onDragCancel = {
                                viewModel.recorder.stopRecording()
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cw = size.width
                    val ch = size.height

                    // Grid crosshair center lines
                    val gridColor = Color(0x15FFFFFF)
                    drawLine(gridColor, Offset(cw / 2f, 0f), Offset(cw / 2f, ch), strokeWidth = 1f)
                    drawLine(gridColor, Offset(0f, ch / 2f), Offset(cw, ch / 2f), strokeWidth = 1f)

                    // Draw recorded points trail
                    val pts = snapshot.points
                    if (pts.size >= 2) {
                        val path = Path()
                        path.moveTo(pts.first().x * cw, pts.first().y * ch)
                        for (i in 1 until pts.size) {
                            path.lineTo(pts[i].x * cw, pts[i].y * ch)
                        }

                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(NeonCyan, NeonPurple),
                                start = Offset(pts.first().x * cw, pts.first().y * ch),
                                end = Offset(pts.last().x * cw, pts.last().y * ch)
                            ),
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )

                        // Start dot
                        drawCircle(NeonEmerald, radius = 10f, center = Offset(pts.first().x * cw, pts.first().y * ch))
                        // End dot
                        drawCircle(NeonRed, radius = 10f, center = Offset(pts.last().x * cw, pts.last().y * ch))
                    } else if (pts.size == 1) {
                        drawCircle(NeonCyan, radius = 12f, center = Offset(pts.first().x * cw, pts.first().y * ch))
                    }
                }

                if (snapshot.points.isEmpty()) {
                    Text(
                        text = "Touch and swipe to draw gesture path",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // 3. Bottom controls
            GamingCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = gestureName,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Save As Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedLabelColor = NeonCyan
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Path Smoothing (Douglas-Peucker)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Reduces redundant points to optimize latency",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = applySmoothing,
                            onCheckedChange = { viewModel.toggleSmoothing() },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeonOutlinedButton(
                            text = "CLEAR",
                            onClick = { viewModel.reset() },
                            icon = Icons.Default.Refresh,
                            color = TextSecondary,
                            modifier = Modifier.weight(0.8f)
                        )
                        NeonButton(
                            text = "SAVE GESTURE",
                            onClick = { viewModel.saveRecordedGesture(onBack) },
                            icon = Icons.Default.Check,
                            color = NeonEmerald,
                            enabled = snapshot.points.isNotEmpty(),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudMetric(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(text = value, fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
    }
}
