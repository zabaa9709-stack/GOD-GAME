package com.gamegesture.app

import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.data.model.NormalizedPoint
import com.gamegesture.app.gesture.CircularGestureGenerator
import com.gamegesture.app.gesture.CoordinateMapper
import com.gamegesture.app.gesture.DispatchStep
import com.gamegesture.app.gesture.GestureEngine
import com.gamegesture.app.gesture.PathSimplifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class GestureEngineUnitTest {

    @Test
    fun testCoordinateMapper() {
        val screenW = 1080
        val screenH = 2400

        // Center normalized 0.5, 0.5 -> 540, 1200
        val pxX = CoordinateMapper.toPixelX(0.5f, screenW)
        val pxY = CoordinateMapper.toPixelY(0.5f, screenH)
        assertEquals(540f, pxX, 0.01f)
        assertEquals(1200f, pxY, 0.01f)

        // Pixel back to normalized
        val normX = CoordinateMapper.toNormalizedX(540f, screenW)
        val normY = CoordinateMapper.toNormalizedY(1200f, screenH)
        assertEquals(0.5f, normX, 0.01f)
        assertEquals(0.5f, normY, 0.01f)

        // Clamping check
        val clampedX = CoordinateMapper.toPixelX(1.5f, screenW)
        assertEquals(screenW.toFloat(), clampedX, 0.01f)
    }

    @Test
    fun testCircularGestureGenerator() {
        val cx = 0.5f
        val cy = 0.5f
        val r = 0.2f
        val count = 36

        val points = CircularGestureGenerator.generateNormalizedPoints(
            centerX = cx,
            centerY = cy,
            radius = r,
            startAngleDeg = 0f,
            sweepAngleDeg = 360f,
            isClockwise = true,
            pointCount = count
        )

        assertEquals(count, points.size)

        // First point should be at angle 0°: (cx + r, cy) = (0.7, 0.5)
        val p0 = points.first()
        assertEquals(0.7f, p0.x, 0.02f)
        assertEquals(0.5f, p0.y, 0.02f)

        // All points should be distance r from center
        for (p in points) {
            val dist = hypot((p.x - cx).toDouble(), (p.y - cy).toDouble()).toFloat()
            assertEquals(r, dist, 0.02f)
        }
    }

    @Test
    fun testPathSimplifier() {
        // Line with redundant collinear points
        val collinear = listOf(
            NormalizedPoint(0.1f, 0.1f),
            NormalizedPoint(0.2f, 0.2f),
            NormalizedPoint(0.3f, 0.3f),
            NormalizedPoint(0.4f, 0.4f),
            NormalizedPoint(0.5f, 0.5f)
        )

        val simplified = PathSimplifier.simplify(collinear, 0.005f)
        // Redundant midpoints should be eliminated, preserving endpoints
        assertEquals(2, simplified.size)
        assertEquals(0.1f, simplified.first().x, 0.001f)
        assertEquals(0.5f, simplified.last().x, 0.001f)
    }

    @Test
    fun testGestureEngineSequenceCompilation() {
        val sequence = GestureSequence(
            name = "Tactical Combo",
            actions = listOf(
                GestureAction.TapAction(0.8f, 0.6f),
                GestureAction.DelayAction(50L),
                GestureAction.SwipeAction(0.8f, 0.5f, 0.3f, 0.5f, 150L)
            ),
            repeatCount = 2,
            delayBetweenRepeatsMs = 40L
        )

        // Repeat 2x: (Tap + Delay + Swipe) + RepeatDelay + (Tap + Delay + Swipe) = 7 steps
        val steps = GestureEngine.compileSequenceSteps(sequence, 1080, 2400)
        assertEquals(7, steps.size)

        assertTrue(steps[0] is DispatchStep.StrokeStep)
        assertTrue(steps[1] is DispatchStep.DelayOnlyStep)
        assertTrue(steps[2] is DispatchStep.StrokeStep)
        assertTrue(steps[3] is DispatchStep.DelayOnlyStep) // repeat pause
        assertTrue(steps[4] is DispatchStep.StrokeStep)
        assertTrue(steps[5] is DispatchStep.DelayOnlyStep)
        assertTrue(steps[6] is DispatchStep.StrokeStep)
    }
}
