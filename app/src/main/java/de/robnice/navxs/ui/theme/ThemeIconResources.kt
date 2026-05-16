package de.robnice.navxs.ui.theme

import de.robnice.navxs.R

fun themeDrawableRes(vectorAssetName: String): Int? = when (vectorAssetName) {
    "CropSquare" -> R.drawable.overlay_recents_square
    "RecentsLinesHorizontal" -> R.drawable.overlay_recents_lines_horizontal
    "RecentsLinesVertical" -> R.drawable.overlay_recents_lines_vertical
    "Layers" -> R.drawable.overlay_recents_layers
    "DashboardCustomize" -> R.drawable.overlay_recents_grid
    "Apps" -> R.drawable.overlay_recents_classic
    "ViewAgenda" -> R.drawable.overlay_recents_outlined
    else -> null
}
