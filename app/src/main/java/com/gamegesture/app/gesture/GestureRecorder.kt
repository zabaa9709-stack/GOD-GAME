package com.gamegesture.app.gesture

import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.data.model.NormalizedPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.hypot

enum class RecorderStatus {
    IDLE,
    RECORDING,
    PAUSED,
    COMPLETED
}

data class RecorderSnapshot(
    val status: RecorderStatus = RecorderStatus.IDLE,
    val points: List<NormalizedPoint> = emptyList(),
    val startTimeMs: Long = 0L,
    val elapsedDurationMs: Long = 0L,
    val totalDistanceNormalized: Float = 0f,
    val pointCount: Int = 0
)

class GestureRecorder {

    private val _snapshot = MutableStateFlow(RecorderSnapshot())
    val snapshot: StateFlow<RecorderSnapshot> = _snapshot.asStateFlow()

    private val currentPoints = mutableListOf<NormalizedPoint>()
    private var recordStartTime = 0L

    fun startRecording() {
        currentPoints.clear()
        recordStartTime = System.currentTimeMillis()
        _snapshot.value = RecorderSnapshot(
            status = RecorderStatus.RECORDING,
            points = emptyList(),
            startTimeMs = recordStartTime,
            elapsedDurationMs = 0L,
            totalDistanceNormalized = 0f,
            pointCount = 0
        )
    }

    fun onTouchDown(normalizedX: Float, normalizedY: Float) {
        if (_snapshot.value.status != RecorderStatus.RECORDING) {
            startRecording()
        }
        val timestamp = System.currentTimeMillis() - recordStartTime
        val point = NormalizedPoint(normalizedX, normalizedY, timestamp)
        currentPoints.add(point)
        updateSnapshot()
    }

    fun onTouchMove(normalizedX: Float, normalizedY: Float) {
        if (_snapshot.value.status != RecorderStatus.RECORDING) return
        val timestamp = System.currentTimeMillis() - recordStartTime

        // Check if movement is significant enough to record
        val last = currentPoints.lastOrNull()
        if (last != null) {
            val dist = hypot((normalizedX - last.x).toDouble(), (normalizedY - last.y).toDouble()).toFloat()
            if (dist < 0.002f) return // filter out negligible jitter
        }

        val point = NormalizedPoint(normalizedX, normalizedY, timestamp)
        currentPoints.add(point)
        updateSnapshot()
    }

    fun onTouchUp(normalizedX: Float, normalizedY: Float) {
        if (_snapshot.value.status != RecorderStatus.RECORDING) return
        val timestamp = System.currentTimeMillis() - recordStartTime
        val point = NormalizedPoint(normalizedX, normalizedY, timestamp)
        currentPoints.add(point)

        val totalDuration = (System.currentTimeMillis() - recordStartTime).coerceAtLeast(50L)
        _snapshot.value = _snapshot.value.copy(
            status = RecorderStatus.COMPLETED,
            points = currentPoints.toList(),
            elapsedDurationMs = totalDuration,
            pointCount = currentPoints.size
        )
    }

    fun stopRecording() {
        if (_snapshot.value.status == RecorderStatus.RECORDING) {
            val totalDuration = (System.currentTimeMillis() - recordStartTime).coerceAtLeast(50L)
            _snapshot.value = _snapshot.value.copy(
                status = RecorderStatus.COMPLETED,
                points = currentPoints.toList(),
                elapsedDurationMs = totalDuration,
                pointCount = currentPoints.size
            )
        }
    }

    fun reset() {
        currentPoints.clear()
        recordStartTime = 0L
        _snapshot.value = RecorderSnapshot()
    }

    fun toGestureSequence(
        name: String,
        description: String = "Recorded freehand gaming gesture",
        applySmoothing: Boolean = true
    ): GestureSequence {
        val points = if (applySmoothing && currentPoints.size > 8) {
            PathSimplifier.simplify(currentPoints, 0.004f)
        } else {
            currentPoints.toList()
        }

        val duration = _snapshot.value.elapsedDurationMs.coerceAtLeast(100L)

        // Classify based on simplified point geometry
        val action = when {
            points.isEmpty() -> GestureAction.TapAction(0.5f, 0.5f)
            points.size == 1 -> GestureAction.TapAction(points[0].x, points[0].y)
            points.size == 2 -> {
                val p1 = points[0]
                val p2 = points[1]
                val dist = hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()
                if (dist < 0.03f) {
                    GestureAction.TapAction(p1.x, p1.y)
                } else {
                    GestureAction.SwipeAction(
                        startX = p1.x,
                        startY = p1.y,
                        endX = p2.x,
                        endY = p2.y,
                        durationMs = duration
                    )
                }
            }
            else -> {
                GestureAction.FreeformAction(
                    points = points,
                    durationMs = duration
                )
            }
        }

        return GestureSequence(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            actions = listOf(action),
            repeatCount = 1,
            isCalibrated = true
        )
    }

    private fun updateSnapshot() {
        var totalDist = 0f
        for (i in 1 until currentPoints.size) {
            val p1 = currentPoints[i - 1]
            val p2 = currentPoints[i]
            totalDist += hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()
        }

        _snapshot.value = RecorderSnapshot(
            status = _snapshot.value.status,
            points = currentPoints.toList(),
            startTimeMs = recordStartTime,
            elapsedDurationMs = System.currentTimeMillis() - recordStartTime,
            totalDistanceNormalized = totalDist,
            pointCount = currentPoints.size
        )
    }
}
