package de.robnice.navxs.domain

import de.robnice.navxs.data.models.AppOverlayConfiguration
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
import de.robnice.navxs.data.models.OverlaySettings

object OverlayConfigurationResolver {
    fun resolve(
        defaultSettings: OverlaySettings,
        packageName: String?,
        appConfigurations: Map<String, AppOverlayConfiguration>
    ): OverlaySettings {
        val appConfiguration = packageName?.let(appConfigurations::get)
        if (appConfiguration?.individualEnabled != true) return defaultSettings

        return defaultSettings.copy(
            buttons = completeButtons(
                source = appConfiguration.buttons,
                fallback = defaultSettings.buttons
            )
        )
    }

    fun copyButtons(settings: OverlaySettings): Map<NavButtonType, OverlayButtonConfig> =
        settings.buttons.mapValues { (type, button) -> button.copy(type = type) }

    private fun completeButtons(
        source: Map<NavButtonType, OverlayButtonConfig>,
        fallback: Map<NavButtonType, OverlayButtonConfig>
    ): Map<NavButtonType, OverlayButtonConfig> = NavButtonType.entries.associateWith { type ->
        source[type]?.copy(type = type) ?: fallback.getValue(type).copy(type = type)
    }
}
