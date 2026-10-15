package com.gamegesture.app.gesture

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.GestureSequence

sealed class DispatchStep {
    data class StrokeStep(
        val path: Path,
        val durationMs: Long,
        val delayBeforeMs: Long,
        val label: String
    ) : DispatchStep()

    data class DelayOnlyStep(
        val delayMs: Long
    ) : DispatchStep()
}

object GestureEngine {

    /**
     * Converts a single GestureAction into a DispatchStep (Path + duration + delay)
     * using current screen dimensions.
     */
    fun createDispatchStep(
        action: GestureAction,
        screenWidth: Int,
        screenHeight: Int
    ): DispatchStep {
        return when (action) {
            is GestureAction.TapAction -> {
                val px = CoordinateMapper.toPixelX(action.x, screenWidth)
                val py = CoordinateMapper.toPixelY(action.y, screenHeight)
                val path = Path().apply {
                    moveTo(px, py)
                    lineTo(px + 1f, py + 1f)
                }
                DispatchStep.StrokeStep(
                    path = path,
                    durationMs = 45L,
                    delayBeforeMs = action.delayMs,
                    label = "Tap at (${(action.x * 100).toInt()}%, ${(action.y * 100).toInt()}%)"
                )
            }

            is GestureAction.LongPressAction -> {
                val px = CoordinateMapper.toPixelX(action.x, screenWidth)
                val py = CoordinateMapper.toPixelY(action.y, screenHeight)
                val path = Path().apply {
                    moveTo(px, py)
                    lineTo(px + 1f, py + 1f)
                }
                DispatchStep.StrokeStep(
                    path = path,
                    durationMs = action.durationMs.coerceIn(100L, 2000L),
                    delayBeforeMs = action.delayMs,
                    label = "Long Press (${action.durationMs}ms)"
                )
            }

            is GestureAction.SwipeAction -> {
                val startX = CoordinateMapper.toPixelX(action.startX, screenWidth)
                val startY = CoordinateMapper.toPixelY(action.startY, screenHeight)
                val endX = CoordinateMapper.toPixelX(action.endX, screenWidth)
                val endY = CoordinateMapper.toPixelY(action.endY, screenHeight)

                val path = Path().apply {
                    moveTo(startX, startY)
                    lineTo(endX, endY)
                }
                DispatchStep.StrokeStep(
                    path = path,
                    durationMs = action.durationMs.coerceIn(50L, 2000L),
                    delayBeforeMs = action.delayMs,
                    label = "Swipe (${action.durationMs}ms)"
                )
            }

            is GestureAction.CircularAction -> {
                val path = CircularGestureGenerator.buildPixelPath(action, screenWidth, screenHeight)
                DispatchStep.StrokeStep(
                    path = path,
                    durationMs = action.durationMs.coerceIn(60L, 3000L),
                    delayBeforeMs = action.delayMs,
                    label = "Circular 360° (${action.durationMs}ms, ${action.pointCount} pts)"
                )
            }

            is GestureAction.FreeformAction -> {
                val path = Path()
                if (action.points.isNotEmpty()) {
                    val p0 = action.points.first()
                    path.moveTo(
                        CoordinateMapper.toPixelX(p0.x, screenWidth),
                        CoordinateMapper.toPixelY(p0.y, screenHeight)
                    )
                    for (i in 1 until action.points.size) {
                        val pt = action.points[i]
                        path.lineTo(
                            CoordinateMapper.toPixelX(pt.x, screenWidth),
                            CoordinateMapper.toPixelY(pt.y, screenHeight)
                        )
                    }
                }
                DispatchStep.StrokeStep(
                    path = path,
                    durationMs = action.durationMs.coerceIn(50L, 5000L),
                    delayBeforeMs = action.delayMs,
                    label = "Freeform Path (${action.points.size} pts, ${action.durationMs}ms)"
                )
            }

            is GestureAction.DelayAction -> {
                DispatchStep.DelayOnlyStep(
                    delayMs = action.delayMs.coerceAtLeast(10L)
                )
            }
        }
    }

    /**
     * Builds the entire sequence of steps across repeats and delays.
     */
    fun compileSequenceSteps(
        sequence: GestureSequence,
        screenWidth: Int,
        screenHeight: Int
    ): List<DispatchStep> {
        val steps = mutableListOf<DispatchStep>()
        val repeats = sequence.repeatCount.coerceIn(1, 10)

        for (r in 0 until repeats) {
            for (action in sequence.actions) {
                steps.add(createDispatchStep(action, screenWidth, screenHeight))
            }
            if (r < repeats - 1 && sequence.delayBetweenRepeatsMs > 0) {
                steps.add(DispatchStep.DelayOnlyStep(sequence.delayBetweenRepeatsMs))
            }
        }

        return steps
    }

    /**
     * Builds an Android Accessibility GestureDescription from a StrokeStep.
     */
    fun buildGestureDescription(strokeStep: DispatchStep.StrokeStep): GestureDescription {
        val builder = GestureDescription.Builder()
        val stroke = GestureDescription.StrokeDescription(
            strokeStep.path,
            0L,
            strokeStep.durationMs
        )
        builder.addStroke(stroke)
        return builder.build()
    }
}
