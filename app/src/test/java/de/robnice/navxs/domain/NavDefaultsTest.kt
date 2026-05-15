package de.robnice.navxs.domain

import com.google.common.truth.Truth.assertThat
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.models.NavButtonType
import org.junit.Test

class NavDefaultsTest {
    @Test
    fun defaultConfigurationContainsExactlyThreeButtons() {
        val settings = NavDefaults.defaultOverlaySettings()

        assertThat(settings.buttons.keys).containsExactly(
            NavButtonType.BACK,
            NavButtonType.HOME,
            NavButtonType.RECENTS
        )
    }

    @Test
    fun defaultConfigurationPreservesFixedTypes() {
        val settings = NavDefaults.defaultOverlaySettings()

        assertThat(settings.buttons.getValue(NavButtonType.BACK).type).isEqualTo(NavButtonType.BACK)
        assertThat(settings.buttons.getValue(NavButtonType.HOME).type).isEqualTo(NavButtonType.HOME)
        assertThat(settings.buttons.getValue(NavButtonType.RECENTS).type).isEqualTo(NavButtonType.RECENTS)
    }
}
