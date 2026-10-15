package com.gamegesture.app.gesture

import android.graphics.Path
import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.NormalizedPoint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CircularGestureGenerator {

    /**
     * Generates a sequence of normalized points forming a circle or arc.
     * x = centerX + radius * cos(angle)
     * y = centerY + radius * sin(angle)
     */
    fun generateNormalizedPoints(
        centerX: Float,
        centerY: Float,
        radius: Float,
        startAngleDeg: Float = 0f,
        sweepAngleDeg: Float = 360f,
        isClockwise: Boolean = true,
        pointCount: Int = 36
    ): List<NormalizedPoint> {
        val count = pointCount.coerceIn(8, 120)
        val points = mutableListOf<NormalizedPoint>()

        val sweep = if (isClockwise) sweepAngleDeg else -sweepAngleDeg
        val step = sweep / (count - 1)

        for (i in 0 until count) {
            val angleDeg = startAngleDeg + (i * step)
            val angleRad = (angleDeg * PI / 180.0).toFloat()

            val nx = centerX + radius * cos(angleRad)
            val ny = centerY + radius * sin(angleRad)

            points.add(NormalizedPoint(x = nx, y = ny))
        }

        return points
    }

    /**
     * Converts a CircularAction into an Android graphics Path using screen pixel dimensions.
     */
    fun buildPixelPath(
        action: GestureAction.CircularAction,
        screenWidth: Int,
        screenHeight: Int
    ): Path {
        val path = Path()
        val points = generateNormalizedPoints(
            centerX = action.centerX,
            centerY = action.centerY,
            radius = action.radius,
            startAngleDeg = action.startAngleDeg,
            sweepAngleDeg = action.sweepAngleDeg,
            isClockwise = action.isClockwise,
            pointCount = action.pointCount
        )

        if (points.isEmpty()) return path

        val firstX = CoordinateMapper.toPixelX(points.first().x, screenWidth)
        val firstY = CoordinateMapper.toPixelY(points.first().y, screenHeight)
        path.moveTo(firstX, firstY)

        for (i in 1 until points.size) {
            val px = CoordinateMapper.toPixelX(points[i].x, screenWidth)
            val py = CoordinateMapper.toPixelY(points[i].y, screenHeight)
            path.lineTo(px, py)
        }

        return path
    }
}
