package de.robnice.navxs.domain

import com.google.common.truth.Truth.assertThat
import de.robnice.navxs.data.models.NavButtonType
import org.junit.Test

class ThemeRegistryTest {
    private val registry = ThemeRegistry()

    @Test
    fun themeListIsFilteredByButtonType() {
        val themes = registry.themesFor(NavButtonType.HOME)

        assertThat(themes).isNotEmpty()
        assertThat(themes.map { it.buttonType }.distinct()).containsExactly(NavButtonType.HOME)
    }

    @Test
    fun missingThemeFallsBackToDefault() {
        val theme = registry.resolve(NavButtonType.RECENTS, "does_not_exist")

        assertThat(theme.id).isEqualTo("recents_square")
    }
}
