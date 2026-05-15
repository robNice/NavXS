package de.robnice.navxs.domain

import com.google.common.truth.Truth.assertThat
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.models.NavButtonType
import org.junit.Test

class ButtonSettingsUseCaseTest {
    private val useCase = ButtonSettingsUseCase()

    @Test
    fun settingsChangeAffectsOnlySelectedButton() {
        val original = NavDefaults.defaultOverlaySettings()

        val updated = useCase.setColor(original, NavButtonType.BACK, 0xFF000000)

        assertThat(updated.buttons.getValue(NavButtonType.BACK).colorArgb).isEqualTo(0xFF000000)
        assertThat(updated.buttons.getValue(NavButtonType.HOME)).isEqualTo(original.buttons.getValue(NavButtonType.HOME))
        assertThat(updated.buttons.getValue(NavButtonType.RECENTS)).isEqualTo(original.buttons.getValue(NavButtonType.RECENTS))
    }

    @Test
    fun lastActiveButtonCannotBeDisabled() {
        val settings = NavDefaults.defaultOverlaySettings().copy(
            buttons = NavDefaults.defaultOverlaySettings().buttons.mapValues { (_, config) ->
                config.copy(active = config.type == NavButtonType.BACK)
            }
        )

        val result = useCase.setActive(settings, NavButtonType.BACK, false)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun opacityIsClamped() {
        val updated = useCase.setOpacity(NavDefaults.defaultOverlaySettings(), NavButtonType.BACK, 2f)

        assertThat(updated.buttons.getValue(NavButtonType.BACK).opacity).isEqualTo(1f)
    }

    @Test
    fun sizeIsClampedToAllowedRange() {
        val updated = useCase.setSizePercent(NavDefaults.defaultOverlaySettings(), NavButtonType.HOME, 5)

        assertThat(updated.buttons.getValue(NavButtonType.HOME).sizePercent).isEqualTo(100)
    }

    @Test
    fun precisionStepFallsBackToAllowedValues() {
        val updated = useCase.setPrecisionStep(NavDefaults.defaultOverlaySettings(), 3)

        assertThat(updated.precisionStepPx).isEqualTo(5)
    }

    @Test
    fun precisionMovesOnlySelectedButton() {
        val settings = NavDefaults.defaultOverlaySettings().copy(selectedButtonType = NavButtonType.RECENTS)

        val updated = useCase.moveBy(settings, NavButtonType.RECENTS, 10f, -5f)

        assertThat(updated.buttons.getValue(NavButtonType.RECENTS).positionXPx).isEqualTo(
            settings.buttons.getValue(NavButtonType.RECENTS).positionXPx + 10
        )
        assertThat(updated.buttons.getValue(NavButtonType.BACK)).isEqualTo(settings.buttons.getValue(NavButtonType.BACK))
    }
}
