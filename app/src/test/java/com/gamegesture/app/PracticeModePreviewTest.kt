package com.gamegesture.app

import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.data.model.NormalizedPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeModePreviewTest {

    @Test
    fun testTotalEstimatedDurationCalculation() {
        val gesture = GestureSequence(
            name = "Combo Sequence",
            actions = listOf(
                GestureAction.TapAction(0.5f, 0.5f, delayMs = 20L), // 20 + 50 = 70ms
                GestureAction.SwipeAction(0.2f, 0.2f, 0.8f, 0.8f, durationMs = 150L, delayMs = 30L), // 30 + 150 = 180ms
                GestureAction.DelayAction(delayMs = 100L) // 100ms
            ),
            repeatCount = 1,
            delayBetweenRepeatsMs = 50L
        )

        // 70 + 180 + 100 = 350ms
        assertEquals(350L, gesture.totalEstimatedDurationMs)
    }

    @Test
    fun testRepeatedSequenceDurationCalculation() {
        val gesture = GestureSequence(
            name = "Repeated Tap",
            actions = listOf(
                GestureAction.TapAction(0.7f, 0.7f, delayMs = 10L) // 10 + 50 = 60ms
            ),
            repeatCount = 4,
            delayBetweenRepeatsMs = 40L
        )

        // (60 * 4) + (3 * 40) = 240 + 120 = 360ms
        assertEquals(360L, gesture.totalEstimatedDurationMs)
    }

    @Test
    fun testAllActionTypesEstimatedDurations() {
        val longPress = GestureSequence(
            name = "Long Press",
            actions = listOf(GestureAction.LongPressAction(0.5f, 0.5f, durationMs = 500L, delayMs = 0L))
        )
        assertEquals(500L, longPress.totalEstimatedDurationMs)

        val circular = GestureSequence(
            name = "360 Circle",
            actions = listOf(GestureAction.CircularAction(durationMs = 280L, delayMs = 20L))
        )
        assertEquals(300L, circular.totalEstimatedDurationMs)

        val freeform = GestureSequence(
            name = "Freeform",
            actions = listOf(
                GestureAction.FreeformAction(
                    points = listOf(NormalizedPoint(0.1f, 0.1f), NormalizedPoint(0.9f, 0.9f)),
                    durationMs = 400L,
                    delayMs = 50L
                )
            )
        )
        assertEquals(450L, freeform.totalEstimatedDurationMs)
    }

    @Test
    fun testEmptyActionsSequenceDuration() {
        val empty = GestureSequence(name = "Empty", actions = emptyList())
        assertEquals(0L, empty.totalEstimatedDurationMs)
    }
}
