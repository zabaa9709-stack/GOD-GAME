package com.gamegesture.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.ui.components.GamingCard
import com.gamegesture.app.ui.components.GamingHeader
import com.gamegesture.app.ui.components.NeonButton
import com.gamegesture.app.ui.components.StatusBadge
import com.gamegesture.app.ui.theme.CyberCardBorder
import com.gamegesture.app.ui.theme.CyberDarkBg
import com.gamegesture.app.ui.theme.CyberSurface
import com.gamegesture.app.ui.theme.NeonCyan
import com.gamegesture.app.ui.theme.NeonEmerald
import com.gamegesture.app.ui.theme.NeonRed
import com.gamegesture.app.ui.theme.TextMuted
import com.gamegesture.app.ui.theme.TextPrimary
import com.gamegesture.app.ui.theme.TextSecondary

@Composable
fun GestureLibraryScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onPractice: (String) -> Unit,
    onCreateNew: () -> Unit,
    viewModel: GestureLibraryViewModel = viewModel()
) {
    val gestures by viewModel.gestures.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var renamingGesture by remember { mutableStateOf<GestureSequence?>(null) }
    var renameInput by remember { mutableStateOf("") }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (renamingGesture != null) {
        AlertDialog(
            onDismissRequest = { renamingGesture = null },
            title = {
                Text(
                    text = "RENAME GESTURE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter a new name for this gesture sequence:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val g = renamingGesture
                        if (g != null && renameInput.isNotBlank()) {
                            viewModel.renameGesture(g.id, renameInput)
                        }
                        renamingGesture = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = CyberDarkBg
                    )
                ) {
                    Text("RENAME", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { renamingGesture = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                ) {
                    Text("CANCEL")
                }
            },
            containerColor = CyberSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Scaffold(
        containerColor = CyberDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GamingHeader(
                title = "GESTURE LIBRARY",
                subtitle = "Manage, rename, and test saved action profiles",
                onBack = onBack,
                actions = {
                    NeonButton(
                        text = "+ NEW",
                        onClick = onCreateNew,
                        icon = Icons.Default.Add,
                        color = NeonCyan
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
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (gestures.isEmpty()) {
                item {
                    GamingCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Gestures in Library",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create customizable touch combo actions or capture raw gestures using the recorder.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            NeonButton(
                                text = "+ CREATE GESTURE",
                                onClick = onCreateNew,
                                color = NeonCyan
                            )
                        }
                    }
                }
            } else {
                items(gestures, key = { it.id }) { item ->
                    LibraryItemCard(
                        gesture = item,
                        onTest = { viewModel.testGesture(item) },
                        onEdit = { onEdit(item.id) },
                        onPractice = { onPractice(item.id) },
                        onRename = {
                            renamingGesture = item
                            renameInput = item.name
                        },
                        onDuplicate = { viewModel.duplicateGesture(item) },
                        onDelete = { viewModel.deleteGesture(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryItemCard(
    gesture: GestureSequence,
    onTest: () -> Unit,
    onEdit: () -> Unit,
    onPractice: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    GamingCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gesture.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${gesture.totalEstimatedDurationMs} ms • ${gesture.actions.size} step(s)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                StatusBadge(
                    label = if (gesture.isCalibrated) "Calibrated ✓" else "Raw",
                    isActive = gesture.isCalibrated,
                    activeColor = NeonEmerald
                )
            }

            if (gesture.description.isNotEmpty()) {
                Text(
                    text = gesture.description,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTest,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f), contentColor = NeonCyan),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TEST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPractice,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald.copy(alpha = 0.2f), contentColor = NeonEmerald),
                    modifier = Modifier.weight(1.1f).height(36.dp)
                ) {
                    Text("PRACTICE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurface, contentColor = TextPrimary),
                    modifier = Modifier.weight(0.9f).height(36.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EDIT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.DriveFileRenameOutline,
                        contentDescription = "Rename",
                        tint = NeonCyan.copy(alpha = 0.8f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
