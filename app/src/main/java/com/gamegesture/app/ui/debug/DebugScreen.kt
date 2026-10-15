package com.gamegesture.app.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamegesture.app.service.GestureExecutionLog
import com.gamegesture.app.ui.components.GamingCard
import com.gamegesture.app.ui.components.GamingHeader
import com.gamegesture.app.ui.components.StatusBadge
import com.gamegesture.app.ui.theme.CyberDarkBg
import com.gamegesture.app.ui.theme.NeonCyan
import com.gamegesture.app.ui.theme.NeonEmerald
import com.gamegesture.app.ui.theme.NeonRed
import com.gamegesture.app.ui.theme.TextMuted
import com.gamegesture.app.ui.theme.TextPrimary
import com.gamegesture.app.ui.theme.TextSecondary

@Composable
fun DebugScreen(
    onBack: () -> Unit,
    viewModel: DebugViewModel = viewModel()
) {
    val isConnected by viewModel.isServiceConnected.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()
    val logs by viewModel.debugLogs.collectAsState()
    val metrics = viewModel.getScreenMetrics()

    Scaffold(
        containerColor = CyberDarkBg,
        topBar = {
            GamingHeader(
                title = "DIAGNOSTICS & DEBUG",
                subtitle = "Real-time dispatch latency and service telemetry",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = TextSecondary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Service Connection Status Card (PRD Section 27)
            item {
                GamingCard(
                    borderColor = if (isConnected) NeonEmerald.copy(alpha = 0.5f) else NeonRed.copy(alpha = 0.5f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACCESSIBILITY SERVICE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(
                                label = if (isConnected) "Connected ✓" else "Disconnected ✗",
                                isActive = isConnected,
                                activeColor = NeonEmerald,
                                inactiveColor = NeonRed
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Active Dispatching State", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = if (isExecuting) "DISPATCHING MOTION..." else "IDLE",
                                color = if (isExecuting) NeonCyan else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 2. Screen Coordinate System Metrics
            item {
                GamingCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "DISPLAY COORDINATE SPACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        DebugRow("Resolution", "${metrics.widthPixels} × ${metrics.heightPixels} px")
                        DebugRow("Aspect Ratio", metrics.aspectRatio)
                        DebugRow("DPI / Scale Factor", "${metrics.densityDpi} DPI (${metrics.density}x)")
                        DebugRow("Overlay Permission", if (viewModel.hasOverlay()) "Granted ✓" else "Missing ✗")
                    }
                }
            }

            // 3. Execution Logs Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISPATCH EVENT LOGS (${logs.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. Log Items
            if (logs.isEmpty()) {
                item {
                    GamingCard {
                        Text(
                            text = "No dispatch events logged yet. Trigger a gesture from Home or the Floating Shortcut to record latency telemetry.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    LogItemCard(log)
                }
            }
        }
    }
}

@Composable
private fun LogItemCard(log: GestureExecutionLog) {
    GamingCard(
        borderColor = if (log.success) NeonEmerald.copy(alpha = 0.3f) else NeonRed.copy(alpha = 0.3f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.gestureName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (log.success) "SUCCESS" else "FAILURE",
                    color = if (log.success) NeonEmerald else NeonRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "${log.stepCount} steps • Duration: ${log.durationMs} ms",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = log.message,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
}
