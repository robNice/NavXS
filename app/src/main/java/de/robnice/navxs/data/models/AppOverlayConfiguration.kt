package de.robnice.navxs.data.models

data class AppOverlayConfiguration(
    val individualEnabled: Boolean,
    val buttons: Map<NavButtonType, OverlayButtonConfig>
)
