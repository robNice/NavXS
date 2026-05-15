package de.robnice.navxs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF165DA8),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF143457),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF0E8DB8),
    background = Color(0xFFF7FAFD),
    onBackground = Color(0xFF0F2032),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF102133)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF57DDFF),
    onPrimary = Color(0xFF082038),
    secondary = Color(0xFF1C314E),
    onSecondary = Color(0xFFEAFBFF),
    tertiary = Color(0xFF9DEEFF),
    background = Color(0xFF09131F),
    onBackground = Color(0xFFEAF7FF),
    surface = Color(0xFF112238),
    onSurface = Color(0xFFEAF7FF)
)

@Composable
fun NavXsTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val colors = if (dynamicColor) {
        if (darkTheme) DarkColors else LightColors
    } else if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
