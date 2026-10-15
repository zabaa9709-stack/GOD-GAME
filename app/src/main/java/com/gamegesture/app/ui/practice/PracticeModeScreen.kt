package com.gamegesture.app.ui.practice

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamegesture.app.ui.components.GamingCard
import com.gamegesture.app.ui.components.GamingHeader
import com.gamegesture.app.ui.components.GesturePreviewCanvas
import com.gamegesture.app.ui.components.NeonButton
import com.gamegesture.app.ui.components.NeonOutlinedButton
import com.gamegesture.app.ui.components.StatusBadge
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

@Composable
fun PracticeModeScreen(
    gestureId: String?,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: PracticeModeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(gestureId) {
        if (!gestureId.isNullOrEmpty()) {
            viewModel.selectGestureById(gestureId)
        }
    }

    LaunchedEffect(state.executionResult) {
        state.executionResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearResult()
        }
    }

    Scaffold(
        containerColor = CyberDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GamingHeader(
                title = "PRACTICE CANVAS",
                subtitle = "Inspect and simulate gesture paths in real time",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.allGestures.isEmpty()) {
                GamingCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Gestures Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create a gesture first to preview and practice it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // 1. Gesture Selector Carousel
                Text(
                    text = "SELECT GESTURE TO TEST",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.allGestures) { item ->
                        val isSelected = item.id == state.selectedGesture?.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan else CyberSurface)
                                .border(1.dp, if (isSelected) NeonCyan else CyberCardBorder, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectGestureById(item.id) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = item.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CyberDarkBg else TextPrimary
                            )
                        }
                    }
                }

                // 2. Large Interactive Preview Canvas
                val selected = state.selectedGesture
                if (selected != null) {
                    GesturePreviewCanvas(
                        sequence = selected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        animate = true,
                        playbackSpeed = state.playbackSpeed
                    )

                    // 3. Playback speed controls
                    GamingCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SIMULATION SPEED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.playbackSpeed}x",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SpeedChip(
                                    speed = 0.5f,
                                    label = "0.5x Slow",
                                    isSelected = state.playbackSpeed == 0.5f,
                                    onClick = { viewModel.setSpeed(0.5f) },
                                    modifier = Modifier.weight(1f)
                                )
                                SpeedChip(
                                    speed = 1.0f,
                                    label = "1.0x Normal",
                                    isSelected = state.playbackSpeed == 1.0f,
                                    onClick = { viewModel.setSpeed(1.0f) },
                                    modifier = Modifier.weight(1f)
                                )
                                SpeedChip(
                                    speed = 1.5f,
                                    label = "1.5x Fast",
                                    isSelected = state.playbackSpeed == 1.5f,
                                    onClick = { viewModel.setSpeed(1.5f) },
                                    modifier = Modifier.weight(1f)
                                )
                                SpeedChip(
                                    speed = 2.0f,
                                    label = "2.0x Blitz",
                                    isSelected = state.playbackSpeed == 2.0f,
                                    onClick = { viewModel.setSpeed(2.0f) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 4. Live Simulation Feedback HUD
                    if (state.simulationStepText != null) {
                        GamingCard(
                            borderColor = if (state.isSimulating) NeonCyan else NeonEmerald
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (state.isSimulating) "IN-APP SIMULATION ACTIVE" else "SIMULATION RESULT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (state.isSimulating) NeonCyan else NeonEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = state.simulationStepText ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (state.isSimulating) {
                                    NeonOutlinedButton(
                                        text = "STOP",
                                        icon = Icons.Default.Stop,
                                        color = NeonRed,
                                        onClick = { viewModel.cancelSimulation() }
                                    )
                                }
                            }
                        }
                    }

                    // 5. Gesture Metrics Card
                    GamingCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total Duration", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "${selected.totalEstimatedDurationMs} ms", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Step Count", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "${selected.actions.size} action(s)", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Repeats", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "${selected.repeatCount}x", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Calibration", color = TextSecondary, fontSize = 13.sp)
                                StatusBadge(
                                    label = if (selected.isCalibrated) "Calibrated ✓" else "Uncalibrated",
                                    isActive = selected.isCalibrated,
                                    activeColor = NeonEmerald
                                )
                            }
                        }
                    }

                    // 6. Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeonButton(
                            text = if (state.isSimulating) "SIMULATING..." else "REPLAY SIMULATION",
                            icon = Icons.Default.Replay,
                            color = NeonCyan,
                            enabled = !state.isSimulating,
                            onClick = { viewModel.replaySimulation() },
                            modifier = Modifier.weight(1.3f)
                        )
                        NeonOutlinedButton(
                            text = "EDIT",
                            icon = Icons.Default.Edit,
                            color = NeonPurple,
                            onClick = { onEdit(selected.id) },
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedChip(
    speed: Float,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) NeonCyan else CyberSurface)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
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
