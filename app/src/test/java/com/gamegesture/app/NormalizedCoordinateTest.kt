package com.gamegesture.app

import com.gamegesture.app.data.model.NormalizedPoint
import com.gamegesture.app.gesture.CoordinateMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NormalizedCoordinateTest {

    @Test
    fun testCenterCoordinateMappingAcrossResolutions() {
        val resolutions = listOf(
            Pair(1080, 1920), // 16:9
            Pair(1080, 2400), // 20:9
            Pair(1440, 3120), // 19.5:9
            Pair(720, 1280)   // 720p
        )

        for ((w, h) in resolutions) {
            val pxX = CoordinateMapper.toPixelX(0.5f, w)
            val pxY = CoordinateMapper.toPixelY(0.5f, h)

            assertEquals(w / 2f, pxX, 0.01f)
            assertEquals(h / 2f, pxY, 0.01f)

            val normX = CoordinateMapper.toNormalizedX(pxX, w)
            val normY = CoordinateMapper.toNormalizedY(pxY, h)

            assertEquals(0.5f, normX, 0.001f)
            assertEquals(0.5f, normY, 0.001f)
        }
    }

    @Test
    fun testEdgeCoordinates() {
        val w = 1080
        val h = 2400

        // Origin (0, 0)
        assertEquals(0f, CoordinateMapper.toPixelX(0f, w), 0.01f)
        assertEquals(0f, CoordinateMapper.toPixelY(0f, h), 0.01f)
        assertEquals(0f, CoordinateMapper.toNormalizedX(0f, w), 0.001f)
        assertEquals(0f, CoordinateMapper.toNormalizedY(0f, h), 0.001f)

        // Bottom right (1.0, 1.0)
        assertEquals(w.toFloat(), CoordinateMapper.toPixelX(1.0f, w), 0.01f)
        assertEquals(h.toFloat(), CoordinateMapper.toPixelY(1.0f, h), 0.01f)
        assertEquals(1.0f, CoordinateMapper.toNormalizedX(w.toFloat(), w), 0.001f)
        assertEquals(1.0f, CoordinateMapper.toNormalizedY(h.toFloat(), h), 0.001f)
    }

    @Test
    fun testOutOfBoundsClamping() {
        val w = 1080
        val h = 2400

        // Negative values clamped to 0
        val negX = CoordinateMapper.toPixelX(-0.5f, w)
        val negY = CoordinateMapper.toPixelY(-0.2f, h)
        assertEquals(0f, negX, 0.01f)
        assertEquals(0f, negY, 0.01f)

        // Values > 1.0 clamped to screen width/height
        val overX = CoordinateMapper.toPixelX(1.8f, w)
        val overY = CoordinateMapper.toPixelY(2.0f, h)
        assertEquals(w.toFloat(), overX, 0.01f)
        assertEquals(h.toFloat(), overY, 0.01f)

        // Pixel to normalized clamping
        assertEquals(0f, CoordinateMapper.toNormalizedX(-100f, w), 0.001f)
        assertEquals(1f, CoordinateMapper.toNormalizedX(2000f, w), 0.001f)
    }

    @Test
    fun testNormalizedPointBoundsValidation() {
        // Valid point within acceptable bounds
        val p = NormalizedPoint(0.75f, 0.25f, 150L)
        assertEquals(0.75f, p.x, 0.001f)
        assertEquals(0.25f, p.y, 0.001f)
        assertEquals(150L, p.timestampMs)

        // Unreasonable out-of-bounds throws IllegalArgumentException
        assertThrows(IllegalArgumentException::class.java) {
            NormalizedPoint(2.5f, 0.5f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NormalizedPoint(0.5f, -1.0f)
        }
    }
}
