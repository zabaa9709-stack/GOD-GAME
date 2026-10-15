package com.gamegesture.app

import com.gamegesture.app.data.model.ActionType
import com.gamegesture.app.gesture.GestureRecorder
import com.gamegesture.app.gesture.RecorderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GestureRecorderTest {

    private lateinit var recorder: GestureRecorder

    @Before
    fun setUp() {
        recorder = GestureRecorder()
    }

    @Test
    fun testInitialState() {
        val snap = recorder.snapshot.value
        assertEquals(RecorderStatus.IDLE, snap.status)
        assertEquals(0, snap.points.size)
        assertEquals(0, snap.pointCount)
    }

    @Test
    fun testTouchDragRecordingCycle() {
        // Touch down
        recorder.onTouchDown(0.2f, 0.2f)
        assertEquals(RecorderStatus.RECORDING, recorder.snapshot.value.status)
        assertEquals(1, recorder.snapshot.value.points.size)

        // Touch moves
        recorder.onTouchMove(0.3f, 0.3f)
        recorder.onTouchMove(0.4f, 0.4f)
        recorder.onTouchMove(0.5f, 0.5f)
        assertEquals(4, recorder.snapshot.value.points.size)

        // Touch up
        recorder.onTouchUp(0.5f, 0.5f)
        assertEquals(RecorderStatus.COMPLETED, recorder.snapshot.value.status)
        assertTrue(recorder.snapshot.value.elapsedDurationMs >= 0)
        assertTrue(recorder.snapshot.value.totalDistanceNormalized > 0.3f)
    }

    @Test
    fun testJitterFiltering() {
        recorder.onTouchDown(0.5f, 0.5f)

        // Very tiny jitter below threshold (<0.002f distance)
        recorder.onTouchMove(0.5005f, 0.5005f)
        // Should not add jitter point
        assertEquals(1, recorder.snapshot.value.points.size)

        // Significant movement
        recorder.onTouchMove(0.55f, 0.55f)
        assertEquals(2, recorder.snapshot.value.points.size)
    }

    @Test
    fun testToGestureSequenceStraightSwipe() {
        recorder.onTouchDown(0.1f, 0.1f)
        for (i in 2..15) {
            val v = i * 0.05f
            recorder.onTouchMove(v, v)
        }
        recorder.onTouchUp(0.8f, 0.8f)

        val sequence = recorder.toGestureSequence(
            name = "Recorded Swipe Path",
            applySmoothing = true
        )

        assertEquals("Recorded Swipe Path", sequence.name)
        assertEquals(1, sequence.actions.size)
        // Straight line simplifies to 2 endpoints with distance > 0.03f, cleanly classified as SWIPE!
        assertEquals(ActionType.SWIPE, sequence.actions[0].type)
        assertTrue("Sequence should be calibrated", sequence.isCalibrated)
    }

    @Test
    fun testToGestureSequenceCurvedFreeform() {
        // Draw an S-curve or arc that cannot be simplified to a single line segment
        recorder.onTouchDown(0.2f, 0.2f)
        recorder.onTouchMove(0.4f, 0.1f)
        recorder.onTouchMove(0.6f, 0.3f)
        recorder.onTouchMove(0.4f, 0.6f)
        recorder.onTouchMove(0.7f, 0.8f)
        recorder.onTouchUp(0.8f, 0.8f)

        val sequence = recorder.toGestureSequence(
            name = "Curved S-Shape",
            applySmoothing = false
        )

        assertEquals("Curved S-Shape", sequence.name)
        assertEquals(1, sequence.actions.size)
        assertEquals(ActionType.FREEFORM, sequence.actions[0].type)
    }

    @Test
    fun testToGestureSequenceTapConversionForMinimalPoints() {
        // Single tap or very small motion
        recorder.onTouchDown(0.6f, 0.6f)
        recorder.onTouchUp(0.6f, 0.6f)

        val sequence = recorder.toGestureSequence(name = "Quick Tap")
        assertEquals(1, sequence.actions.size)
        // Since points <= 2, it converts into a TapAction!
        assertEquals(ActionType.TAP, sequence.actions[0].type)
    }

    @Test
    fun testRecorderReset() {
        recorder.onTouchDown(0.2f, 0.2f)
        recorder.onTouchMove(0.4f, 0.4f)
        recorder.onTouchUp(0.6f, 0.6f)
        assertEquals(RecorderStatus.COMPLETED, recorder.snapshot.value.status)

        recorder.reset()
        assertEquals(RecorderStatus.IDLE, recorder.snapshot.value.status)
        assertEquals(0, recorder.snapshot.value.points.size)
    }
}
