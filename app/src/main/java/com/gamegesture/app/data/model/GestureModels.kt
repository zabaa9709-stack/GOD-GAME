package com.gamegesture.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class NormalizedPoint(
    val x: Float,
    val y: Float,
    val timestampMs: Long = 0L
) {
    init {
        require(x in -0.5f..1.5f) { "Normalized X out of reasonable bounds: $x" }
        require(y in -0.5f..1.5f) { "Normalized Y out of reasonable bounds: $y" }
    }
}

@Serializable
enum class ActionType {
    TAP,
    LONG_PRESS,
    SWIPE,
    CIRCULAR,
    FREEFORM,
    DELAY
}

@Serializable
sealed class GestureAction {
    abstract val type: ActionType
    abstract val delayMs: Long

    @Serializable
    @SerialName("tap")
    data class TapAction(
        val x: Float,
        val y: Float,
        override val delayMs: Long = 0L
    ) : GestureAction() {
        override val type: ActionType = ActionType.TAP
    }

    @Serializable
    @SerialName("long_press")
    data class LongPressAction(
        val x: Float,
        val y: Float,
        val durationMs: Long = 400L,
        override val delayMs: Long = 0L
    ) : GestureAction() {
        override val type: ActionType = ActionType.LONG_PRESS
    }

    @Serializable
    @SerialName("swipe")
    data class SwipeAction(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val durationMs: Long = 180L,
        override val delayMs: Long = 0L
    ) : GestureAction() {
        override val type: ActionType = ActionType.SWIPE
    }

    @Serializable
    @SerialName("circular")
    data class CircularAction(
        val centerX: Float = 0.5f,
        val centerY: Float = 0.5f,
        val radius: Float = 0.25f,
        val startAngleDeg: Float = 0f,
        val sweepAngleDeg: Float = 360f,
        val isClockwise: Boolean = true,
        val durationMs: Long = 300L,
        val pointCount: Int = 36,
        override val delayMs: Long = 0L
    ) : GestureAction() {
        override val type: ActionType = ActionType.CIRCULAR
    }

    @Serializable
    @SerialName("freeform")
    data class FreeformAction(
        val points: List<NormalizedPoint>,
        val durationMs: Long = 300L,
        override val delayMs: Long = 0L
    ) : GestureAction() {
        override val type: ActionType = ActionType.FREEFORM
    }

    @Serializable
    @SerialName("delay")
    data class DelayAction(
        override val delayMs: Long = 100L
    ) : GestureAction() {
        override val type: ActionType = ActionType.DELAY
    }
}

@Serializable
data class GestureSequence(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val actions: List<GestureAction> = emptyList(),
    val repeatCount: Int = 1,
    val delayBetweenRepeatsMs: Long = 50L,
    val isCalibrated: Boolean = false,
    val profileId: String = "default",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalEstimatedDurationMs: Long
        get() {
            var sum = 0L
            for (action in actions) {
                sum += action.delayMs
                when (action) {
                    is GestureAction.TapAction -> sum += 50L
                    is GestureAction.LongPressAction -> sum += action.durationMs
                    is GestureAction.SwipeAction -> sum += action.durationMs
                    is GestureAction.CircularAction -> sum += action.durationMs
                    is GestureAction.FreeformAction -> sum += action.durationMs
                    is GestureAction.DelayAction -> sum += 0L
                }
            }
            return (sum * repeatCount.coerceAtLeast(1)) + ((repeatCount - 1).coerceAtLeast(0) * delayBetweenRepeatsMs)
        }
}
