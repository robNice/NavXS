package de.robnice.navxs.overlay

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

internal object OverlayViewport {
    fun metrics(context: Context): DisplayMetrics {
        val resourceMetrics = context.resources.displayMetrics
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return DisplayMetrics().apply {
                widthPixels = resourceMetrics.widthPixels
                heightPixels = resourceMetrics.heightPixels
                density = resourceMetrics.density
                densityDpi = resourceMetrics.densityDpi
                scaledDensity = resourceMetrics.scaledDensity
                xdpi = resourceMetrics.xdpi
                ydpi = resourceMetrics.ydpi
            }
        }
        val windowManager = context.getSystemService(WindowManager::class.java)
        val bounds = windowManager?.currentWindowMetrics?.bounds
        return DisplayMetrics().apply {
            widthPixels = bounds?.width() ?: resourceMetrics.widthPixels
            heightPixels = bounds?.height() ?: resourceMetrics.heightPixels
            density = resourceMetrics.density
            densityDpi = resourceMetrics.densityDpi
            scaledDensity = resourceMetrics.scaledDensity
            xdpi = resourceMetrics.xdpi
            ydpi = resourceMetrics.ydpi
        }
    }
}
