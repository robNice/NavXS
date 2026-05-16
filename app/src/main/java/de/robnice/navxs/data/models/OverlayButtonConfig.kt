package de.robnice.navxs.data.models

data class OverlayButtonConfig(
    val type: NavButtonType,
    val active: Boolean,
    val colorArgb: Long,
    val opacity: Float,
    val sizePercent: Int,
    val backgroundColorArgb: Long,
    val backgroundOpacity: Float,
    val backgroundSizePercent: Int,
    val backgroundSoftnessPercent: Int,
    val positionXPx: Int,
    val positionYPx: Int,
    val themeId: String
)
