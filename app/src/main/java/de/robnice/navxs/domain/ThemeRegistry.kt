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
            ButtonTheme("back_01", NavButtonType.BACK, "01", "ArrowBack01", "←"),
            ButtonTheme("back_02", NavButtonType.BACK, "02", "ArrowBack02", "←"),
            ButtonTheme("back_03", NavButtonType.BACK, "03", "ArrowBack03", "←"),
            ButtonTheme("back_04", NavButtonType.BACK, "04", "ArrowBack04", "←"),
            ButtonTheme("back_05", NavButtonType.BACK, "05", "ArrowBack05", "←"),
            ButtonTheme("back_06", NavButtonType.BACK, "06", "ArrowBack06", "←"),
            ButtonTheme("back_07", NavButtonType.BACK, "07", "ArrowBack07", "←"),
            ButtonTheme("back_08", NavButtonType.BACK, "08", "ArrowBack08", "←"),
            ButtonTheme("back_09", NavButtonType.BACK, "09", "ArrowBack09", "←"),
            ButtonTheme("back_10", NavButtonType.BACK, "10", "ArrowBack10", "←"),
            ButtonTheme("back_11", NavButtonType.BACK, "11", "ArrowBack11", "←"),
            ButtonTheme("back_12", NavButtonType.BACK, "12", "ArrowBack12", "←"),
            ButtonTheme("home_circle", NavButtonType.HOME, "circle", "RadioButtonUnchecked", "○"),
            ButtonTheme("home_circle_filled", NavButtonType.HOME, "circle_filled", "Circle", "●"),
            ButtonTheme("home_rounded_square", NavButtonType.HOME, "rounded_square", "HomeSquircleOutline", "▢"),
            ButtonTheme("home_rounded_square_filled", NavButtonType.HOME, "rounded_square_filled", "HomeSquircleFilled", "■"),
            ButtonTheme("home_house", NavButtonType.HOME, "house", "HomeOutlined", "⌂"),
            ButtonTheme("home_house_filled", NavButtonType.HOME, "house_filled", "Home", "⌂"),
            ButtonTheme("home_01", NavButtonType.HOME, "01", "Home01", "▬"),
            ButtonTheme("home_02", NavButtonType.HOME, "02", "Home02", "▬"),
            ButtonTheme("home_03", NavButtonType.HOME, "03", "Home03", "◉"),
            ButtonTheme("home_04", NavButtonType.HOME, "04", "Home04", "◉"),
            ButtonTheme("home_05", NavButtonType.HOME, "05", "Home05", "⌂"),
            ButtonTheme("home_06", NavButtonType.HOME, "06", "Home06", "⌂"),
            ButtonTheme("home_07", NavButtonType.HOME, "07", "Home07", "⌂"),
            ButtonTheme("home_08", NavButtonType.HOME, "08", "Home08", "⌂"),
            ButtonTheme("home_09", NavButtonType.HOME, "09", "Home09", "•"),
            ButtonTheme("home_10", NavButtonType.HOME, "10", "Home10", "●"),
            ButtonTheme("home_11", NavButtonType.HOME, "11", "Home11", "⌂"),
            ButtonTheme("recents_square", NavButtonType.RECENTS, "square", "CropSquare", "□"),
            ButtonTheme("recents_01", NavButtonType.RECENTS, "01", "Recents01", "■"),
            ButtonTheme("recents_lines_horizontal", NavButtonType.RECENTS, "lines_horizontal", "RecentsLinesHorizontal", "☰"),
            ButtonTheme("recents_lines_vertical", NavButtonType.RECENTS, "lines_vertical", "RecentsLinesVertical", "⋮"),
            ButtonTheme("recents_grid", NavButtonType.RECENTS, "grid", "DashboardCustomize", "▦"),
            ButtonTheme("recents_02", NavButtonType.RECENTS, "02", "Recents02", "▦"),
            ButtonTheme("recents_03", NavButtonType.RECENTS, "03", "Recents03", "□"),
            ButtonTheme("recents_04", NavButtonType.RECENTS, "04", "Recents04", "■"),
            ButtonTheme("recents_05", NavButtonType.RECENTS, "05", "Recents05", "▣"),
            ButtonTheme("recents_06", NavButtonType.RECENTS, "06", "Recents06", "□"),
            ButtonTheme("recents_07", NavButtonType.RECENTS, "07", "Recents07", "▣"),
            ButtonTheme("recents_08", NavButtonType.RECENTS, "08", "Recents08", "□"),
            ButtonTheme("recents_09", NavButtonType.RECENTS, "09", "Recents09", "⋮"),
            ButtonTheme("recents_10", NavButtonType.RECENTS, "10", "Recents10", "□"),
            ButtonTheme("recents_11", NavButtonType.RECENTS, "11", "Recents11", "▣"),
            ButtonTheme("recents_12", NavButtonType.RECENTS, "12", "Recents12", "▣")
        )
    }
}
