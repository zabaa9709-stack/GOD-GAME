package com.gamegesture.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.data.model.NormalizedPoint
import com.gamegesture.app.gesture.CircularGestureGenerator
import com.gamegesture.app.ui.theme.CyberCardBg
import com.gamegesture.app.ui.theme.CyberCardBorder
import com.gamegesture.app.ui.theme.CyberDarkBg
import com.gamegesture.app.ui.theme.NeonCoral
import com.gamegesture.app.ui.theme.NeonCyan
import com.gamegesture.app.ui.theme.NeonEmerald
import com.gamegesture.app.ui.theme.NeonPurple
import com.gamegesture.app.ui.theme.NeonRed
import com.gamegesture.app.ui.theme.TextMuted
import com.gamegesture.app.ui.theme.TextPrimary
import com.gamegesture.app.ui.theme.TextSecondary

@Composable
fun GamingCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberCardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = NeonCyan,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = CyberDarkBg,
            disabledContainerColor = CyberCardBorder,
            disabledContentColor = TextMuted
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun NeonOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = NeonCyan,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (enabled) color else CyberCardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color,
            disabledContentColor = TextMuted
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun StatusBadge(
    label: String,
    isActive: Boolean,
    activeColor: Color = NeonEmerald,
    inactiveColor: Color = NeonRed
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CyberDarkBg,
        border = BorderStroke(1.dp, if (isActive) activeColor.copy(alpha = 0.5f) else CyberCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) activeColor else inactiveColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) TextPrimary else TextSecondary
            )
        }
    }
}

@Composable
fun GamingHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        if (actions != null) {
            actions()
        }
    }
}

@Composable
fun GesturePreviewCanvas(
    sequence: GestureSequence,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    showGrid: Boolean = true,
    playbackSpeed: Float = 1.0f
) {
    val baseDuration = sequence.totalEstimatedDurationMs.coerceIn(800L, 4000L)
    val effectiveDuration = (baseDuration / playbackSpeed.coerceAtLeast(0.2f)).toInt().coerceIn(300, 8000)

    val infiniteTransition = rememberInfiniteTransition(label = "gesture_anim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = effectiveDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF07090D))
            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Subtle grid lines
            if (showGrid) {
                val gridColor = Color(0x15FFFFFF)
                val stroke = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                for (i in 1..3) {
                    val x = canvasW * (i / 4f)
                    val y = canvasH * (i / 4f)
                    drawLine(gridColor, Offset(x, 0f), Offset(x, canvasH), strokeWidth = 1f, pathEffect = stroke.pathEffect)
                    drawLine(gridColor, Offset(0f, y), Offset(canvasW, y), strokeWidth = 1f, pathEffect = stroke.pathEffect)
                }
            }

            // 2. Render each action in sequence
            for (action in sequence.actions) {
                when (action) {
                    is GestureAction.TapAction -> {
                        val px = action.x * canvasW
                        val py = action.y * canvasH

                        // Concentric pulse circle
                        drawCircle(
                            color = NeonEmerald.copy(alpha = 0.25f),
                            radius = 24f,
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = NeonEmerald,
                            radius = 8f,
                            center = Offset(px, py)
                        )
                    }

                    is GestureAction.LongPressAction -> {
                        val px = action.x * canvasW
                        val py = action.y * canvasH
                        drawCircle(
                            color = NeonCoral.copy(alpha = 0.3f),
                            radius = 32f,
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = NeonCoral,
                            radius = 12f,
                            center = Offset(px, py)
                        )
                    }

                    is GestureAction.SwipeAction -> {
                        val sx = action.startX * canvasW
                        val sy = action.startY * canvasH
                        val ex = action.endX * canvasW
                        val ey = action.endY * canvasH

                        // Draw path line with gradient
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(NeonCyan, NeonPurple),
                                start = Offset(sx, sy),
                                end = Offset(ex, ey)
                            ),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )

                        // Start dot
                        drawCircle(NeonCyan, radius = 9f, center = Offset(sx, sy))
                        // End arrow / dot
                        drawCircle(NeonPurple, radius = 9f, center = Offset(ex, ey))

                        // Animated touch head
                        if (animate) {
                            val curX = sx + (ex - sx) * animProgress
                            val curY = sy + (ey - sy) * animProgress
                            drawCircle(Color.White, radius = 7f, center = Offset(curX, curY))
                            drawCircle(NeonCyan.copy(alpha = 0.5f), radius = 16f, center = Offset(curX, curY))
                        }
                    }

                    is GestureAction.CircularAction -> {
                        val cx = action.centerX * canvasW
                        val cy = action.centerY * canvasH
                        val r = action.radius * canvasW.coerceAtMost(canvasH)

                        // Center indicator
                        drawCircle(Color(0x3300E5FF), radius = 6f, center = Offset(cx, cy))

                        // Circle path
                        val points = CircularGestureGenerator.generateNormalizedPoints(
                            centerX = action.centerX,
                            centerY = action.centerY,
                            radius = action.radius,
                            startAngleDeg = action.startAngleDeg,
                            sweepAngleDeg = action.sweepAngleDeg,
                            isClockwise = action.isClockwise,
                            pointCount = action.pointCount
                        )

                        if (points.isNotEmpty()) {
                            val path = Path()
                            val first = points.first()
                            path.moveTo(first.x * canvasW, first.y * canvasH)

                            for (i in 1 until points.size) {
                                val pt = points[i]
                                path.lineTo(pt.x * canvasW, pt.y * canvasH)
                            }

                            drawPath(
                                path = path,
                                brush = Brush.sweepGradient(
                                    colors = listOf(NeonCyan, NeonPurple, NeonCyan),
                                    center = Offset(cx, cy)
                                ),
                                style = Stroke(width = 6f, cap = StrokeCap.Round)
                            )

                            // Start dot
                            drawCircle(NeonEmerald, radius = 8f, center = Offset(first.x * canvasW, first.y * canvasH))

                            // Animated moving head around circle
                            if (animate && points.size > 1) {
                                val index = ((points.size - 1) * animProgress).toInt().coerceIn(0, points.size - 1)
                                val curPt = points[index]
                                val curX = curPt.x * canvasW
                                val curY = curPt.y * canvasH
                                drawCircle(Color.White, radius = 7f, center = Offset(curX, curY))
                                drawCircle(NeonCyan.copy(alpha = 0.5f), radius = 16f, center = Offset(curX, curY))
                            }
                        }
                    }

                    is GestureAction.FreeformAction -> {
                        if (action.points.size >= 2) {
                            val path = Path()
                            val p0 = action.points.first()
                            path.moveTo(p0.x * canvasW, p0.y * canvasH)
                            for (i in 1 until action.points.size) {
                                val pt = action.points[i]
                                path.lineTo(pt.x * canvasW, pt.y * canvasH)
                            }
                            drawPath(
                                path = path,
                                color = NeonCyan,
                                style = Stroke(width = 6f, cap = StrokeCap.Round)
                            )

                            // Start & end points
                            val end = action.points.last()
                            drawCircle(NeonEmerald, radius = 8f, center = Offset(p0.x * canvasW, p0.y * canvasH))
                            drawCircle(NeonRed, radius = 8f, center = Offset(end.x * canvasW, end.y * canvasH))

                            // Animated follower
                            if (animate) {
                                val index = ((action.points.size - 1) * animProgress).toInt().coerceIn(0, action.points.size - 1)
                                val cur = action.points[index]
                                drawCircle(Color.White, radius = 7f, center = Offset(cur.x * canvasW, cur.y * canvasH))
                            }
                        }
                    }

                    is GestureAction.DelayAction -> {
                        // Delay step doesn't draw pixels, but adds to timeline
                    }
                }
            }
        }
    }
}
