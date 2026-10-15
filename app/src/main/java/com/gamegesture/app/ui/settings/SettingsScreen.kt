package com.gamegesture.app.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
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

    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Profile JSON", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    label = { Text("Paste JSON Payload") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder
                    )
                )
            },
            confirmButton = {
                NeonButton(
                    text = "IMPORT",
                    onClick = {
                        viewModel.importProfile(importText)
                        showImportDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = CyberCardBg
        )
    }

    Scaffold(
        containerColor = CyberDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GamingHeader(
                title = "SETTINGS",
                subtitle = "Engine parameters, device profiles, and compliance",
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
            // 1. General Preferences
            GamingCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "FEEDBACK & INTERACTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )

                    SettingToggleRow(
                        title = "Tactile Haptic Feedback",
                        subtitle = "Vibrate on shortcut trigger and gesture execution",
                        isChecked = state.settings.hapticFeedback,
                        onCheckedChange = { viewModel.toggleHaptic(it) }
                    )

                    SettingToggleRow(
                        title = "Gesture Path Smoothing",
                        subtitle = "Ramer-Douglas-Peucker point filtering for low latency",
                        isChecked = state.settings.gestureSmoothing,
                        onCheckedChange = { viewModel.toggleSmoothing(it) }
                    )

                    SettingToggleRow(
                        title = "Developer Debug Logging",
                        subtitle = "Keep real-time records of dispatch duration and status",
                        isChecked = state.settings.debugMode,
                        onCheckedChange = { viewModel.toggleDebug(it) }
                    )
                }
            }

            // 2. Profile Export & Import (PRD Section 24)
            GamingCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "PROFILE BACKUP & SHARING",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Share calibrated gesture profiles between devices using lightweight JSON payloads.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NeonOutlinedButton(
                            text = "EXPORT PROFILE",
                            icon = Icons.Default.FileUpload,
                            color = NeonCyan,
                            onClick = {
                                viewModel.exportActiveProfile()
                                state.exportedJson?.let {
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clip.setPrimaryClip(ClipData.newPlainText("GameGesture Profile", it))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        NeonOutlinedButton(
                            text = "IMPORT PROFILE",
                            icon = Icons.Default.FileDownload,
                            color = NeonPurple,
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (state.exportedJson != null) {
                        OutlinedTextField(
                            value = state.exportedJson ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Exported JSON (Copied to Clipboard)") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonEmerald,
                                unfocusedBorderColor = CyberCardBorder
                            )
                        )
                    }
                }
            }

            // 3. Android System Permissions Quick Navigation
            GamingCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SYSTEM PERMISSIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )

                    NeonOutlinedButton(
                        text = "ACCESSIBILITY SETTINGS",
                        onClick = { PermissionUtils.openAccessibilitySettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    NeonOutlinedButton(
                        text = "OVERLAY PERMISSION SETTINGS",
                        onClick = { PermissionUtils.openOverlaySettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 4. Safety & Fair Play Compliance (PRD Section 25)
            GamingCard(borderColor = CyberCardBorder) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SAFETY & COMPLIANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCoral,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GameGesture does NOT modify game memory, binary files, or network packets, and does not use root exploits or hidden private APIs. It functions strictly through official Android AccessibilityService and Overlay APIs.\n\nPlease note: Some online competitive games may prohibit automated input or floating overlays. Use responsibly in accordance with game rules.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
        )
    }
}
