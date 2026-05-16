package de.robnice.navxs.domain

import de.robnice.navxs.data.models.ButtonTheme
import de.robnice.navxs.data.models.NavButtonType

class ThemeRegistry {
    fun themesFor(type: NavButtonType): List<ButtonTheme> = themes.filter { it.buttonType == type }

    fun fallbackTheme(type: NavButtonType): ButtonTheme = themesFor(type).first()

    fun resolve(type: NavButtonType, themeId: String): ButtonTheme {
        return themesFor(type).firstOrNull { it.id == themeId } ?: fallbackTheme(type)
    }

    companion object {
        val themes = listOf(
            ButtonTheme("back_new", NavButtonType.BACK, "new", "ArrowBackNew", "◁"),
            ButtonTheme("back_new_filled", NavButtonType.BACK, "new_filled", "ArrowBackNewFilled", "◀"),
            ButtonTheme("back_ios_new", NavButtonType.BACK, "ios_new", "ArrowBackIosNew", "❮"),
            ButtonTheme("back_classic", NavButtonType.BACK, "classic", "ArrowBack", "←"),
            ButtonTheme("home_circle", NavButtonType.HOME, "circle", "RadioButtonUnchecked", "○"),
            ButtonTheme("home_circle_filled", NavButtonType.HOME, "circle_filled", "Circle", "●"),
            ButtonTheme("home_rounded_square", NavButtonType.HOME, "rounded_square", "HomeSquircleOutline", "▢"),
            ButtonTheme("home_rounded_square_filled", NavButtonType.HOME, "rounded_square_filled", "HomeSquircleFilled", "■"),
            ButtonTheme("home_house", NavButtonType.HOME, "house", "HomeOutlined", "⌂"),
            ButtonTheme("home_house_filled", NavButtonType.HOME, "house_filled", "Home", "⌂"),
            ButtonTheme("recents_square", NavButtonType.RECENTS, "square", "CropSquare", "□"),
            ButtonTheme("recents_lines_horizontal", NavButtonType.RECENTS, "lines_horizontal", "RecentsLinesHorizontal", "☰"),
            ButtonTheme("recents_lines_vertical", NavButtonType.RECENTS, "lines_vertical", "RecentsLinesVertical", "⋮"),
            ButtonTheme("recents_grid", NavButtonType.RECENTS, "grid", "DashboardCustomize", "▦")
        )
    }
}
