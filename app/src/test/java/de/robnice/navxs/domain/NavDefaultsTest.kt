package de.robnice.navxs.domain

import android.util.DisplayMetrics
import com.google.common.truth.Truth.assertThat
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.models.NavButtonType
import org.junit.Test

class NavDefaultsTest {
    private val metrics = DisplayMetrics().apply {
        widthPixels = 320
        heightPixels = 640
        density = 1f
    }

    @Test
    fun defaultConfigurationContainsExactlyThreeButtons() {
        val settings = NavDefaults.defaultOverlaySettings(metrics)

        assertThat(settings.buttons.keys).containsExactly(
            NavButtonType.BACK,
            NavButtonType.HOME,
            NavButtonType.RECENTS
        )
    }

    @Test
    fun defaultConfigurationPreservesFixedTypes() {
        val settings = NavDefaults.defaultOverlaySettings(metrics)

        assertThat(settings.buttons.getValue(NavButtonType.BACK).type).isEqualTo(NavButtonType.BACK)
        assertThat(settings.buttons.getValue(NavButtonType.HOME).type).isEqualTo(NavButtonType.HOME)
        assertThat(settings.buttons.getValue(NavButtonType.RECENTS).type).isEqualTo(NavButtonType.RECENTS)
    }

    @Test
    fun defaultPositionsUseTouchTargetRatherThanIconSize() {
        val positions = NavDefaults.defaultButtonPositions(
            displayMetrics = metrics,
            sizePercentByType = mapOf(
                NavButtonType.BACK to 300,
                NavButtonType.HOME to 100,
                NavButtonType.RECENTS to 100
            )
        )

        assertThat(positions.getValue(NavButtonType.BACK).first).isEqualTo(9)
    }
}
