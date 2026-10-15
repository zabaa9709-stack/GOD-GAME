package com.gamegesture.app.ui.shortcut

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
import com.gamegesture.app.ui.theme.NeonCoral
import com.gamegesture.app.ui.theme.NeonCyan
import com.gamegesture.app.ui.theme.NeonEmerald
import com.gamegesture.app.ui.theme.NeonPurple
import com.gamegesture.app.ui.theme.NeonRed
import com.gamegesture.app.ui.theme.TextMuted
import com.gamegesture.app.ui.theme.TextPrimary
import com.gamegesture.app.ui.theme.TextSecondary
import com.gamegesture.app.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutConfigScreen(
    onBack: () -> Unit,
    viewModel: ShortcutViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    var expandedDropdown by remember { mutableStateOf(false) }
    val assignedGesture = state.allGestures.find { it.id == state.config.gestureSequenceId }

    Scaffold(
        containerColor = CyberDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GamingHeader(
                title = "SHORTCUT CONFIG",
                subtitle = "Customize floating overlay button appearance and target gesture",
                onBack = onBack,
                actions = {
                    NeonButton(
                        text = "SAVE",
                        onClick = { viewModel.saveConfig() },
                        icon = Icons.Default.Check,
                        color = NeonEmerald
                    )
                }
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
            // 1. Live Interactive Preview of the Floating Button
            GamingCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OVERLAY BUTTON PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val activeColor = try {
                        Color(android.graphics.Color.parseColor(state.config.colorHex))
                    } catch (e: Exception) {
                        NeonCyan
                    }

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF07090D))
                            .border(1.dp, CyberCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(state.config.sizeDp.dp)) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val r = (size.width / 2f) - 4f
                            val alpha = state.config.opacity.coerceIn(0.2f, 1f)

                            // Background
                            drawCircle(
                                color = Color(0xFF0D0F14).copy(alpha = alpha * 0.85f),
                                radius = r,
                                center = Offset(cx, cy)
                            )
                            // Outer ring
                            drawCircle(
                                color = activeColor.copy(alpha = alpha),
                                radius = r,
                                center = Offset(cx, cy),
                                style = Stroke(width = 4f)
                            )
                            // Crosshair arms
                            val arm = r * 0.45f
                            drawLine(
                                color = activeColor.copy(alpha = alpha),
                                start = Offset(cx - arm, cy),
                                end = Offset(cx + arm, cy),
                                strokeWidth = 3f
                            )
                            drawLine(
                                color = activeColor.copy(alpha = alpha),
                                start = Offset(cx, cy - arm),
                                end = Offset(cx, cy + arm),
                                strokeWidth = 3f
                            )
                            drawCircle(
                                color = activeColor.copy(alpha = alpha),
                                radius = arm * 0.4f,
                                center = Offset(cx, cy),
                                style = Stroke(width = 2.5f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap triggers: ${assignedGesture?.name ?: "None"}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // 2. Gesture Assignment Selector
            GamingCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ASSIGNED GESTURE ACTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = assignedGesture?.name ?: "Select a gesture...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Trigger Action") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedLabelColor = NeonCyan
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            state.allGestures.forEach { gesture ->
                                DropdownMenuItem(
                                    text = { Text("${gesture.name} (${gesture.totalEstimatedDurationMs} ms)") },
                                    onClick = {
                                        viewModel.updateAssignedGesture(gesture.id)
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Styling & Customization
            GamingCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "BUTTON APPEARANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )

                    // Size Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Button Size", fontSize = 12.sp, color = TextSecondary)
                            Text("${state.config.sizeDp} dp", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = state.config.sizeDp.toFloat(),
                            onValueChange = { viewModel.updateSize(it.toInt()) },
                            valueRange = 40f..76f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }

                    // Opacity Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Opacity", fontSize = 12.sp, color = TextSecondary)
                            Text("${(state.config.opacity * 100).toInt()}%", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = state.config.opacity,
                            onValueChange = { viewModel.updateOpacity(it) },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }

                    // Color theme picker
                    Column {
                        Text("Neon Accent Theme", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf(
                                "#00E5FF" to NeonCyan,
                                "#9D00FF" to NeonPurple,
                                "#00E676" to NeonEmerald,
                                "#FF6D00" to NeonCoral,
                                "#FF1744" to NeonRed
                            ).forEach { (hex, col) ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(
                                            2.dp,
                                            if (state.config.colorHex.equals(hex, ignoreCase = true)) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { viewModel.updateColor(hex) }
                                )
                            }
                        }
                    }

                    // Lock position toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Lock Position", style = MaterialTheme.typography.bodyLarge)
                            Text("Prevent accidental dragging during high-intensity games", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = state.config.isLocked,
                            onCheckedChange = { viewModel.toggleLock(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                        )
                    }
                }
            }

            // 4. Service Toggle Button
            NeonButton(
                text = if (state.isOverlayActive) "STOP FLOATING SHORTCUT" else "LAUNCH FLOATING SHORTCUT",
                icon = Icons.Default.PowerSettingsNew,
                color = if (state.isOverlayActive) NeonRed else NeonEmerald,
                onClick = {
                    if (!PermissionUtils.hasOverlayPermission(context)) {
                        PermissionUtils.openOverlaySettings(context)
                    } else {
                        viewModel.toggleOverlay()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
