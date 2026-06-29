package de.robnice.navxs.domain

import android.util.DisplayMetrics
import com.google.common.truth.Truth.assertThat
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.models.NavButtonType
import org.junit.Test

class ButtonSettingsUseCaseTest {
    private val useCase = ButtonSettingsUseCase()
    private val testMetrics = DisplayMetrics().apply {
        widthPixels = 1080
        heightPixels = 2400
        density = 1f
    }

    @Test
    fun settingsChangeAffectsOnlySelectedButton() {
        val original = defaultSettings()

        val updated = useCase.setColor(original, NavButtonType.BACK, 0xFF000000)

        assertThat(updated.buttons.getValue(NavButtonType.BACK).colorArgb).isEqualTo(0xFF000000)
        assertThat(updated.buttons.getValue(NavButtonType.HOME)).isEqualTo(original.buttons.getValue(NavButtonType.HOME))
        assertThat(updated.buttons.getValue(NavButtonType.RECENTS)).isEqualTo(original.buttons.getValue(NavButtonType.RECENTS))
    }

    @Test
    fun lastActiveButtonCannotBeDisabled() {
        val settings = defaultSettings().copy(
            buttons = defaultSettings().buttons.mapValues { (_, config) ->
                config.copy(active = config.type == NavButtonType.BACK)
            }
        )

        val result = useCase.setActive(settings, NavButtonType.BACK, false)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun opacityIsClamped() {
        val updated = useCase.setOpacity(defaultSettings(), NavButtonType.BACK, 2f)

        assertThat(updated.buttons.getValue(NavButtonType.BACK).opacity).isEqualTo(1f)
    }

    @Test
    fun sizeIsClampedToAllowedRange() {
        val updated = useCase.setSizePercent(
            defaultSettings(),
            NavButtonType.HOME,
            5,
            density = 1f
        )

        assertThat(updated.buttons.getValue(NavButtonType.HOME).sizePercent).isEqualTo(100)
    }

    @Test
    fun growingLargeButtonKeepsTouchTargetCenterStable() {
        val settings = defaultSettings()
        val original = settings.buttons.getValue(NavButtonType.HOME)

        val updated = useCase.setSizePercent(settings, NavButtonType.HOME, 300, density = 1f)

        assertThat(updated.buttons.getValue(NavButtonType.HOME).positionXPx).isEqualTo(original.positionXPx - 20)
        assertThat(updated.buttons.getValue(NavButtonType.HOME).positionYPx).isEqualTo(original.positionYPx - 20)
    }

    @Test
    fun growingWithinMinimumTouchTargetDoesNotShiftPosition() {
        val settings = defaultSettings()
        val original = settings.buttons.getValue(NavButtonType.BACK)

        val updated = useCase.setSizePercent(settings, NavButtonType.BACK, 150, density = 1f)

        assertThat(updated.buttons.getValue(NavButtonType.BACK).positionXPx).isEqualTo(original.positionXPx)
        assertThat(updated.buttons.getValue(NavButtonType.BACK).positionYPx).isEqualTo(original.positionYPx)
    }

    @Test
    fun precisionStepFallsBackToAllowedValues() {
        val updated = useCase.setPrecisionStep(defaultSettings(), 3)

        assertThat(updated.precisionStepPx).isEqualTo(5)
    }

    @Test
    fun precisionMovesOnlySelectedButton() {
        val settings = defaultSettings().copy(selectedButtonType = NavButtonType.RECENTS)

        val updated = useCase.moveBy(settings, NavButtonType.RECENTS, 10f, -5f)

        assertThat(updated.buttons.getValue(NavButtonType.RECENTS).positionXPx).isEqualTo(
            settings.buttons.getValue(NavButtonType.RECENTS).positionXPx + 10
        )
        assertThat(updated.buttons.getValue(NavButtonType.BACK)).isEqualTo(settings.buttons.getValue(NavButtonType.BACK))
    }

    @Test
    fun resetPositionRespectsCurrentButtonSize() {
        val settings = defaultSettings().copy(
            buttons = defaultSettings().buttons.toMutableMap().apply {
                this[NavButtonType.HOME] = getValue(NavButtonType.HOME).copy(
                    sizePercent = 300,
                    positionXPx = 123,
                    positionYPx = 456
                )
            }
        )

        val updated = useCase.resetPosition(settings, NavButtonType.HOME, testMetrics)
        val expected = NavDefaults.defaultButtonPositions(
            displayMetrics = testMetrics,
            sizePercentByType = updated.buttons.mapValues { it.value.sizePercent }
        ).getValue(NavButtonType.HOME)

        assertThat(updated.buttons.getValue(NavButtonType.HOME).positionXPx).isEqualTo(expected.first)
        assertThat(updated.buttons.getValue(NavButtonType.HOME).positionYPx).isEqualTo(expected.second)
    }

    private fun defaultSettings() = NavDefaults.defaultOverlaySettings(testMetrics)
}
