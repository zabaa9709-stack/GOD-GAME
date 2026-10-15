package com.gamegesture.app.gesture

import com.gamegesture.app.data.model.NormalizedPoint
import kotlin.math.abs
import kotlin.math.hypot

object PathSimplifier {

    /**
     * Simplifies a list of normalized points using the Ramer-Douglas-Peucker algorithm.
     * epsilon: tolerance in normalized space (e.g. 0.005f ~ 5-10 pixels on 1080p display).
     */
    fun simplify(points: List<NormalizedPoint>, epsilon: Float = 0.005f): List<NormalizedPoint> {
        if (points.size <= 2) return points

        var maxDistance = 0f
        var maxIndex = 0

        val first = points.first()
        val last = points.last()

        for (i in 1 until points.size - 1) {
            val dist = perpendicularDistance(points[i], first, last)
            if (dist > maxDistance) {
                maxDistance = dist
                maxIndex = i
            }
        }

        return if (maxDistance > epsilon) {
            val left = simplify(points.subList(0, maxIndex + 1), epsilon)
            val right = simplify(points.subList(maxIndex, points.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(first, last)
        }
    }

    private fun perpendicularDistance(
        pt: NormalizedPoint,
        lineStart: NormalizedPoint,
        lineEnd: NormalizedPoint
    ): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y

        val mag = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        if (mag == 0f) {
            return hypot((pt.x - lineStart.x).toDouble(), (pt.y - lineStart.y).toDouble()).toFloat()
        }

        val numerator = abs(dy * pt.x - dx * pt.y + lineEnd.x * lineStart.y - lineEnd.y * lineStart.x)
        return numerator / mag
    }
}
