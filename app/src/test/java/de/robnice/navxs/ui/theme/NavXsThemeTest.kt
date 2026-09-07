package de.robnice.navxs.ui.theme

import com.google.common.truth.Truth.assertThat
import de.robnice.navxs.data.models.AppThemeMode
import org.junit.Test

class NavXsThemeTest {
    @Test
    fun systemModeFollowsSystemTheme() {
        assertThat(resolveDarkTheme(AppThemeMode.SYSTEM, systemInDarkTheme = false)).isFalse()
        assertThat(resolveDarkTheme(AppThemeMode.SYSTEM, systemInDarkTheme = true)).isTrue()
    }

    @Test
    fun explicitModesIgnoreSystemTheme() {
        assertThat(resolveDarkTheme(AppThemeMode.LIGHT, systemInDarkTheme = true)).isFalse()
        assertThat(resolveDarkTheme(AppThemeMode.DARK, systemInDarkTheme = false)).isTrue()
    }
}
