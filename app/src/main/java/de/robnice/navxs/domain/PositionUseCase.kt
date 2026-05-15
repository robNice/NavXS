package de.robnice.navxs.domain

import android.graphics.Rect
import de.robnice.navxs.data.models.OverlayButtonConfig
import kotlin.math.max

class PositionUseCase {
    fun clampToBounds(config: OverlayButtonConfig, bounds: Rect, baseSizePx: Int): OverlayButtonConfig {
        val size = max((baseSizePx * config.sizePercent) / 100, 1)
        val maxX = max(bounds.right - size, bounds.left)
        val maxY = max(bounds.bottom - size, bounds.top)
        return config.copy(
            positionXPx = config.positionXPx.coerceIn(bounds.left, maxX),
            positionYPx = config.positionYPx.coerceIn(bounds.top, maxY)
        )
    }
}
