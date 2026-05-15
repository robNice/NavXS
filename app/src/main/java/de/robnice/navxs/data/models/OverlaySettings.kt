package de.robnice.navxs.data.models

data class OverlaySettings(
    val selectedButtonType: NavButtonType,
    val editMode: Boolean,
    val precisionStepPx: Int,
    val buttons: Map<NavButtonType, OverlayButtonConfig>
)
