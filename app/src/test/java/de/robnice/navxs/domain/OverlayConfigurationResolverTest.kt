package de.robnice.navxs.domain

import com.google.common.truth.Truth.assertThat
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.models.AppOverlayConfiguration
import de.robnice.navxs.data.models.NavButtonType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OverlayConfigurationResolverTest {
    @Test
    fun disabledIndividualConfigurationUsesDefaultLayout() {
        val defaults = NavDefaults.defaultOverlaySettings()
        val appButtons = defaults.buttons.toMutableMap().apply {
            this[NavButtonType.BACK] = getValue(NavButtonType.BACK).copy(colorArgb = 0xFF123456)
        }

        val resolved = OverlayConfigurationResolver.resolve(
            defaultSettings = defaults,
            packageName = "example.app",
            appConfigurations = mapOf(
                "example.app" to AppOverlayConfiguration(
                    individualEnabled = false,
                    buttons = appButtons
                )
            )
        )

        assertThat(resolved.buttons).isEqualTo(defaults.buttons)
    }

    @Test
    fun enabledIndividualConfigurationReplacesCompleteButtonLayout() {
        val defaults = NavDefaults.defaultOverlaySettings()
        val appButtons = defaults.buttons.mapValues { (type, button) ->
            button.copy(
                colorArgb = 0xFF123456 + type.ordinal,
                positionXPx = button.positionXPx + 50,
                active = type != NavButtonType.RECENTS
            )
        }

        val resolved = OverlayConfigurationResolver.resolve(
            defaultSettings = defaults,
            packageName = "example.app",
            appConfigurations = mapOf(
                "example.app" to AppOverlayConfiguration(
                    individualEnabled = true,
                    buttons = appButtons
                )
            )
        )

        assertThat(resolved.buttons).isEqualTo(appButtons)
        assertThat(resolved.selectedButtonType).isEqualTo(defaults.selectedButtonType)
        assertThat(resolved.precisionStepPx).isEqualTo(defaults.precisionStepPx)
    }

    @Test
    fun incompleteIndividualConfigurationFallsBackPerButton() {
        val defaults = NavDefaults.defaultOverlaySettings()
        val customBack = defaults.buttons.getValue(NavButtonType.BACK).copy(colorArgb = 0xFF123456)

        val resolved = OverlayConfigurationResolver.resolve(
            defaultSettings = defaults,
            packageName = "example.app",
            appConfigurations = mapOf(
                "example.app" to AppOverlayConfiguration(
                    individualEnabled = true,
                    buttons = mapOf(NavButtonType.BACK to customBack)
                )
            )
        )

        assertThat(resolved.buttons.getValue(NavButtonType.BACK)).isEqualTo(customBack)
        assertThat(resolved.buttons.getValue(NavButtonType.HOME))
            .isEqualTo(defaults.buttons.getValue(NavButtonType.HOME))
        assertThat(resolved.buttons.getValue(NavButtonType.RECENTS))
            .isEqualTo(defaults.buttons.getValue(NavButtonType.RECENTS))
    }

    @Test
    fun copiedButtonsDoNotShareMutableMapWithSource() {
        val defaults = NavDefaults.defaultOverlaySettings()

        val copied = OverlayConfigurationResolver.copyButtons(defaults)
        val changed = copied.toMutableMap().apply {
            this[NavButtonType.HOME] = getValue(NavButtonType.HOME).copy(sizePercent = 220)
        }

        assertThat(defaults.buttons.getValue(NavButtonType.HOME).sizePercent).isEqualTo(100)
        assertThat(changed.getValue(NavButtonType.HOME).sizePercent).isEqualTo(220)
    }
}
