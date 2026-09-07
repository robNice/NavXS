package de.robnice.navxs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.graphics.Color
import de.robnice.navxs.data.models.AppThemeMode

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

/**
 * Whether the resolved app colour scheme is dark. Unlike [isSystemInDarkTheme] this also honours a
 * scheme the user forced in the settings, so screenshots and other theme-specific assets match.
 */
val LocalNavXsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun NavXsTheme(
    appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = resolveDarkTheme(
        appThemeMode = appThemeMode,
        systemInDarkTheme = isSystemInDarkTheme()
    )
    val colors = if (dynamicColor) {
        if (darkTheme) DarkColors else LightColors
    } else if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        // The forced app colour scheme has to drive the system bar icons as well, otherwise light
        // icons stay on a light background when the system itself is in dark mode.
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowInsetsControllerCompat(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(LocalNavXsDarkTheme provides darkTheme) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}

internal fun resolveDarkTheme(
    appThemeMode: AppThemeMode,
    systemInDarkTheme: Boolean
): Boolean = when (appThemeMode) {
    AppThemeMode.SYSTEM -> systemInDarkTheme
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
}
