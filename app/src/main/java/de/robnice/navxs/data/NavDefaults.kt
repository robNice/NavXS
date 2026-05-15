package de.robnice.navxs.data

import android.util.DisplayMetrics
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
import de.robnice.navxs.data.models.OverlaySettings
import kotlin.math.max

object NavDefaults {
    const val DefaultOpacity = 1f
    const val DefaultSizePercent = 100
    const val DefaultPrecisionStepPx = 5

    private const val DefaultBackColor = 0xFF4F7FFF
    private const val DefaultHomeColor = 0xFF20B38E
    private const val DefaultRecentsColor = 0xFFF27B35

    fun defaultButtonConfig(
        type: NavButtonType,
        displayMetrics: DisplayMetrics = android.content.res.Resources.getSystem().displayMetrics
    ): OverlayButtonConfig {
        val color: Long
        val themeId: String
        when (type) {
            NavButtonType.BACK -> {
                color = DefaultBackColor
                themeId = "back_new"
            }
            NavButtonType.HOME -> {
                color = DefaultHomeColor
                themeId = "home_circle"
            }
            NavButtonType.RECENTS -> {
                color = DefaultRecentsColor
                themeId = "recents_square"
            }
        }
        val positions = defaultButtonPositions(displayMetrics)
        val (x, y) = positions.getValue(type)
        return OverlayButtonConfig(
            type = type,
            active = true,
            colorArgb = color,
            opacity = DefaultOpacity,
            sizePercent = DefaultSizePercent,
            positionXPx = x,
            positionYPx = y,
            themeId = themeId
        )
    }

    fun defaultOverlaySettings(
        displayMetrics: DisplayMetrics = android.content.res.Resources.getSystem().displayMetrics
    ): OverlaySettings {
        val buttons = NavButtonType.entries.associateWith { defaultButtonConfig(it, displayMetrics) }
        return OverlaySettings(
            selectedButtonType = NavButtonType.BACK,
            editMode = false,
            precisionStepPx = DefaultPrecisionStepPx,
            buttons = buttons
        )
    }

    fun defaultButtonPositions(
        displayMetrics: DisplayMetrics,
        sizePercentByType: Map<NavButtonType, Int> = NavButtonType.entries.associateWith { DefaultSizePercent }
    ): Map<NavButtonType, Pair<Int, Int>> {
        val viewportWidthPx = displayMetrics.widthPixels
        val viewportHeightPx = displayMetrics.heightPixels
        val density = displayMetrics.density
        val bottomMarginPx = (18 * density).toInt()
        val anchorFractions = mapOf(
            NavButtonType.BACK to 0.18f,
            NavButtonType.HOME to 0.5f,
            NavButtonType.RECENTS to 0.82f
        )
        return NavButtonType.entries.associateWith { type ->
            val sizePercent = sizePercentByType.getValue(type)
            val iconPx = max(((32 * sizePercent) / 100f * density).toInt(), (16 * density).toInt())
            val x = (viewportWidthPx * anchorFractions.getValue(type) - (iconPx / 2f)).toInt()
            val y = (viewportHeightPx - iconPx - bottomMarginPx.toFloat()).toInt()
            x to y
        }
    }
}
