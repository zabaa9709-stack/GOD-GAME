package com.gamegesture.app.gesture

import android.graphics.PointF
import com.gamegesture.app.data.model.NormalizedPoint

object CoordinateMapper {

    /**
     * Converts normalized coordinate (0.0 .. 1.0) to screen pixel coordinate.
     */
    fun toPixelX(normalizedX: Float, screenWidth: Int): Float {
        return (normalizedX * screenWidth).coerceIn(0f, screenWidth.toFloat())
    }

    fun toPixelY(normalizedY: Float, screenHeight: Int): Float {
        return (normalizedY * screenHeight).coerceIn(0f, screenHeight.toFloat())
    }

    fun toPixelPoint(point: NormalizedPoint, screenWidth: Int, screenHeight: Int): PointF {
        return PointF(
            toPixelX(point.x, screenWidth),
            toPixelY(point.y, screenHeight)
        )
    }

    /**
     * Converts screen pixel coordinate to normalized coordinate (0.0 .. 1.0).
     */
    fun toNormalizedX(pixelX: Float, screenWidth: Int): Float {
        if (screenWidth <= 0) return 0f
        return (pixelX / screenWidth).coerceIn(0f, 1f)
    }

    fun toNormalizedY(pixelY: Float, screenHeight: Int): Float {
        if (screenHeight <= 0) return 0f
        return (pixelY / screenHeight).coerceIn(0f, 1f)
    }

    fun toNormalizedPoint(pixelX: Float, pixelY: Float, screenWidth: Int, screenHeight: Int, timestampMs: Long = 0L): NormalizedPoint {
        return NormalizedPoint(
            x = toNormalizedX(pixelX, screenWidth),
            y = toNormalizedY(pixelY, screenHeight),
            timestampMs = timestampMs
        )
    }
}
