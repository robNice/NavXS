package de.robnice.navxs.ui.theme

import de.robnice.navxs.R

fun themeDrawableRes(vectorAssetName: String): Int? = when (vectorAssetName) {
    "ArrowBackNew" -> R.drawable.overlay_arrow_back_new
    "ArrowBackNewFilled" -> R.drawable.overlay_arrow_back_new_filled
    "ArrowBackIosNew" -> R.drawable.overlay_arrow_back_ios_new
    "ArrowBack" -> R.drawable.overlay_arrow_back_classic
    "RadioButtonUnchecked" -> R.drawable.overlay_home_circle
    "Circle" -> R.drawable.overlay_home_circle_filled
    "HomeSquircleOutline" -> R.drawable.overlay_home_squircle_outline
    "HomeSquircleFilled" -> R.drawable.overlay_home_squircle_filled
    "HomeOutlined" -> R.drawable.overlay_home_house
    "Home" -> R.drawable.overlay_home_house_filled
    "CropSquare" -> R.drawable.overlay_recents_square
    "RecentsLinesHorizontal" -> R.drawable.overlay_recents_lines_horizontal
    "RecentsLinesVertical" -> R.drawable.overlay_recents_lines_vertical
    "Layers" -> R.drawable.overlay_recents_layers
    "DashboardCustomize" -> R.drawable.overlay_recents_grid
    "Apps" -> R.drawable.overlay_recents_classic
    "ViewAgenda" -> R.drawable.overlay_recents_outlined
    else -> null
}
