package de.robnice.navxs.domain

import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
import de.robnice.navxs.data.models.OverlaySettings
import kotlin.math.roundToInt

class ButtonSettingsUseCase {
    fun selectButton(settings: OverlaySettings, type: NavButtonType): OverlaySettings {
        return settings.copy(selectedButtonType = type)
    }

    fun setActive(settings: OverlaySettings, type: NavButtonType, active: Boolean): Result<OverlaySettings> {
        val currentlyActive = settings.buttons.values.count { it.active }
        if (!active && settings.buttons[type]?.active == true && currentlyActive == 1) {
            return Result.failure(IllegalStateException("last_active_button"))
        }
        return Result.success(updateButton(settings, type) { it.copy(active = active) })
    }

    fun setColor(settings: OverlaySettings, type: NavButtonType, colorArgb: Long): OverlaySettings {
        return updateButton(settings, type) { it.copy(colorArgb = colorArgb) }
    }

    fun setOpacity(settings: OverlaySettings, type: NavButtonType, opacity: Float): OverlaySettings {
        return updateButton(settings, type) { it.copy(opacity = opacity.coerceIn(0f, 1f)) }
    }

    fun setSizePercent(settings: OverlaySettings, type: NavButtonType, sizePercent: Int): OverlaySettings {
        return updateButton(settings, type) { it.copy(sizePercent = sizePercent.coerceIn(100, 300)) }
    }

    fun setBackgroundColor(settings: OverlaySettings, type: NavButtonType, colorArgb: Long): OverlaySettings {
        return updateButton(settings, type) { it.copy(backgroundColorArgb = colorArgb) }
    }

    fun setBackgroundOpacity(settings: OverlaySettings, type: NavButtonType, opacity: Float): OverlaySettings {
        return updateButton(settings, type) { it.copy(backgroundOpacity = opacity.coerceIn(0f, 1f)) }
    }

    fun setBackgroundSizePercent(settings: OverlaySettings, type: NavButtonType, sizePercent: Int): OverlaySettings {
        return updateButton(settings, type) { it.copy(backgroundSizePercent = sizePercent.coerceIn(100, 300)) }
    }

    fun setBackgroundSoftnessPercent(settings: OverlaySettings, type: NavButtonType, softnessPercent: Int): OverlaySettings {
        return updateButton(settings, type) { it.copy(backgroundSoftnessPercent = softnessPercent.coerceIn(0, 100)) }
    }

    fun setTheme(settings: OverlaySettings, type: NavButtonType, themeId: String): OverlaySettings {
        return updateButton(settings, type) { it.copy(themeId = themeId) }
    }

    fun setEditMode(settings: OverlaySettings, editMode: Boolean): OverlaySettings = settings.copy(editMode = editMode)

    fun setPrecisionStep(settings: OverlaySettings, stepPx: Int): OverlaySettings {
        val safeStep = when (stepPx) {
            1, 5, 10 -> stepPx
            else -> NavDefaults.DefaultPrecisionStepPx
        }
        return settings.copy(precisionStepPx = safeStep)
    }

    fun resetPosition(settings: OverlaySettings, type: NavButtonType): OverlaySettings {
        return updateButton(settings, type) {
            NavDefaults.defaultButtonConfig(type).copy(
                active = it.active,
                colorArgb = it.colorArgb,
                opacity = it.opacity,
                sizePercent = it.sizePercent,
                backgroundColorArgb = it.backgroundColorArgb,
                backgroundOpacity = it.backgroundOpacity,
                backgroundSizePercent = it.backgroundSizePercent,
                backgroundSoftnessPercent = it.backgroundSoftnessPercent,
                themeId = it.themeId
            )
        }
    }

    fun moveBy(settings: OverlaySettings, type: NavButtonType, deltaX: Float, deltaY: Float): OverlaySettings {
        return updateButton(settings, type) {
            it.copy(
                positionXPx = (it.positionXPx + deltaX).roundToInt(),
                positionYPx = (it.positionYPx + deltaY).roundToInt()
            )
        }
    }

    fun setPosition(settings: OverlaySettings, type: NavButtonType, x: Int, y: Int): OverlaySettings {
        return updateButton(settings, type) {
            it.copy(
                positionXPx = x,
                positionYPx = y
            )
        }
    }

    fun setPositions(
        settings: OverlaySettings,
        positions: Map<NavButtonType, Pair<Int, Int>>
    ): OverlaySettings {
        val updatedButtons = settings.buttons.mapValues { (type, button) ->
            positions[type]?.let { (x, y) ->
                button.copy(positionXPx = x, positionYPx = y)
            } ?: button
        }
        return settings.copy(buttons = updatedButtons)
    }

    private fun updateButton(
        settings: OverlaySettings,
        type: NavButtonType,
        transform: (OverlayButtonConfig) -> OverlayButtonConfig
    ): OverlaySettings {
        val current = settings.buttons[type] ?: return settings
        return settings.copy(buttons = settings.buttons + (type to transform(current)))
    }
}
