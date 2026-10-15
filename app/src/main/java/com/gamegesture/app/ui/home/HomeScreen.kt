package com.gamegesture.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.ui.components.GamingCard
import com.gamegesture.app.ui.components.NeonButton
import com.gamegesture.app.ui.components.NeonOutlinedButton
import com.gamegesture.app.ui.components.StatusBadge
import com.gamegesture.app.ui.navigation.Screen
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

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.lastExecutedMessage) {
        state.lastExecutedMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = CyberDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HomeTopBar(
                onSettingsClick = { onNavigate(Screen.Settings.route) },
                onDebugClick = { onNavigate(Screen.Debug.route) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Permission status warnings if not granted
            if (!state.isAccessibilityEnabled || !state.hasOverlayPermission) {
                item {
                    PermissionSetupBanner(
                        accessibilityGranted = state.isAccessibilityEnabled,
                        overlayGranted = state.hasOverlayPermission,
                        onFixAccessibility = { PermissionUtils.openAccessibilitySettings(context) },
                        onFixOverlay = { PermissionUtils.openOverlaySettings(context) }
                    )
                }
            }

            // 2. Engine status & floating shortcut toggle card
            item {
                EngineControlCard(
                    isOverlayActive = state.isOverlayActive,
                    hasOverlayPermission = state.hasOverlayPermission,
                    screenResolution = state.screenResolutionText,
                    onToggleOverlay = {
                        if (!state.hasOverlayPermission) {
                            PermissionUtils.openOverlaySettings(context)
                        } else {
                            viewModel.toggleOverlay()
                        }
                    },
                    onConfigureShortcut = { onNavigate(Screen.ShortcutConfig.route) }
                )
            }

            // 3. Main Action Grid
            item {
                Text(
                    text = "QUICK ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                ActionButtonsGrid(
                    onCreateClick = { onNavigate(Screen.Creator.createRoute()) },
                    onRecordClick = { onNavigate(Screen.Recorder.route) },
                    onCalibrateClick = { onNavigate(Screen.Calibration.route) },
                    onPracticeClick = { onNavigate(Screen.Practice.createRoute()) }
                )
            }

            // 4. Gestures Library Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MY GESTURES (${state.gestures.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "VIEW ALL",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate(Screen.Library.route) }
                    )
                }
            }

            // 5. Saved Gestures list
            if (state.gestures.isEmpty()) {
                item {
                    GamingCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Gestures Created",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            NeonButton(
                                text = "+ CREATE FIRST GESTURE",
                                onClick = { onNavigate(Screen.Creator.createRoute()) }
                            )
                        }
                    }
                }
            } else {
                items(state.gestures, key = { it.id }) { gesture ->
                    GestureItemCard(
                        gesture = gesture,
                        onTestClick = {
                            viewModel.executeGestureQuick(gesture)
                        },
                        onEditClick = {
                            onNavigate(Screen.Creator.createRoute(gesture.id))
                        },
                        onPracticeClick = {
                            onNavigate(Screen.Practice.createRoute(gesture.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    onSettingsClick: () -> Unit,
    onDebugClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideogameAsset,
                    contentDescription = null,
                    tint = CyberDarkBg,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "GAMEGESTURE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = TextPrimary
                )
                Text(
                    text = "ENGINE v1.0 • ACCESSIBILITY DISPATCH",
                    fontSize = 10.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row {
            IconButton(onClick = onDebugClick) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "Diagnostics",
                    tint = TextSecondary
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PermissionSetupBanner(
    accessibilityGranted: Boolean,
    overlayGranted: Boolean,
    onFixAccessibility: () -> Unit,
    onFixOverlay: () -> Unit
) {
    GamingCard(borderColor = NeonCoral.copy(alpha = 0.5f)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NeonCoral,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PERMISSIONS REQUIRED",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonCoral
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "GameGesture needs Accessibility to dispatch calibrated touch motions and Overlay to show the shortcut trigger button.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!accessibilityGranted) {
                    NeonOutlinedButton(
                        text = "ENABLE ACCESSIBILITY",
                        onClick = onFixAccessibility,
                        color = NeonCoral,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (!overlayGranted) {
                    NeonOutlinedButton(
                        text = "GRANT OVERLAY",
                        onClick = onFixOverlay,
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineControlCard(
    isOverlayActive: Boolean,
    hasOverlayPermission: Boolean,
    screenResolution: String,
    onToggleOverlay: () -> Unit,
    onConfigureShortcut: () -> Unit
) {
    GamingCard(
        borderColor = if (isOverlayActive) NeonEmerald.copy(alpha = 0.6f) else CyberCardBorder
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FLOATING SHORTCUT OVERLAY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Screen: $screenResolution",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                StatusBadge(
                    label = if (isOverlayActive) "ACTIVE" else "OFFLINE",
                    isActive = isOverlayActive,
                    activeColor = NeonEmerald,
                    inactiveColor = TextMuted
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeonButton(
                    text = if (isOverlayActive) "STOP OVERLAY" else "LAUNCH OVERLAY",
                    onClick = onToggleOverlay,
                    icon = Icons.Default.PowerSettingsNew,
                    color = if (isOverlayActive) NeonRed else NeonEmerald,
                    modifier = Modifier.weight(1f)
                )
                NeonOutlinedButton(
                    text = "CONFIGURE",
                    onClick = onConfigureShortcut,
                    icon = Icons.Default.ControlCamera,
                    color = NeonCyan,
                    modifier = Modifier.weight(0.9f)
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsGrid(
    onCreateClick: () -> Unit,
    onRecordClick: () -> Unit,
    onCalibrateClick: () -> Unit,
    onPracticeClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "Create Gesture",
                subtitle = "360° circle & custom steps",
                icon = Icons.Default.Add,
                accentColor = NeonCyan,
                modifier = Modifier.weight(1f),
                onClick = onCreateClick
            )
            ActionTile(
                title = "Gesture Recorder",
                subtitle = "Draw & capture live path",
                icon = Icons.Default.FiberManualRecord,
                accentColor = NeonRed,
                modifier = Modifier.weight(1f),
                onClick = onRecordClick
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                title = "Calibrate Device",
                subtitle = "Map screen coordinates",
                icon = Icons.Default.CropRotate,
                accentColor = NeonPurple,
                modifier = Modifier.weight(1f),
                onClick = onCalibrateClick
            )
            ActionTile(
                title = "Practice Mode",
                subtitle = "Simulate & test motions",
                icon = Icons.Default.PlayCircle,
                accentColor = NeonEmerald,
                modifier = Modifier.weight(1f),
                onClick = onPracticeClick
            )
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun GestureItemCard(
    gesture: GestureSequence,
    onTestClick: () -> Unit,
    onEditClick: () -> Unit,
    onPracticeClick: () -> Unit
) {
    GamingCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gesture.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${gesture.totalEstimatedDurationMs} ms • ${gesture.actions.size} actions",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                StatusBadge(
                    label = if (gesture.isCalibrated) "Calibrated ✓" else "Uncalibrated",
                    isActive = gesture.isCalibrated,
                    activeColor = NeonCyan,
                    inactiveColor = TextMuted
                )
            }

            if (gesture.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = gesture.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTestClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan.copy(alpha = 0.2f),
                        contentColor = NeonCyan
                    ),
                    modifier = Modifier.weight(1.1f).height(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("DISPATCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                Button(
                    onClick = onPracticeClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonEmerald.copy(alpha = 0.2f),
                        contentColor = NeonEmerald
                    ),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text("PRACTICE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberSurface,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier.weight(0.8f).height(38.dp)
                ) {
                    Text("EDIT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
