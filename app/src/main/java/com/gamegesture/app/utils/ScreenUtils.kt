package com.gamegesture.app.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.view.WindowManager

data class ScreenMetricsInfo(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val density: Float,
    val aspectRatio: String,
    val isLandscape: Boolean
)

object ScreenUtils {

    fun getScreenMetrics(context: Context): ScreenMetricsInfo {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val width: Int
        val height: Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val bounds = windowMetrics.bounds
            width = bounds.width()
            height = bounds.height()
        } else {
            val display = wm.defaultDisplay
            val size = Point()
            display.getRealSize(size)
            width = size.x
            height = size.y
        }

        val dm = context.resources.displayMetrics
        val dpi = dm.densityDpi
        val density = dm.density
        val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val gcdVal = gcd(width, height)
        val aspect = "${width / gcdVal}:${height / gcdVal}"

        return ScreenMetricsInfo(
            widthPixels = width,
            heightPixels = height,
            densityDpi = dpi,
            density = density,
            aspectRatio = aspect,
            isLandscape = isLandscape
        )
    }

    private fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) {
            val t = y
            y = x % y
            x = t
        }
        return if (x == 0) 1 else x
    }
}
