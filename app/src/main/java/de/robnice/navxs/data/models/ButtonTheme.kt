package de.robnice.navxs.data.models

data class ButtonTheme(
    val id: String,
    val buttonType: NavButtonType,
    val label: String,
    val vectorAssetName: String,
    val fallbackText: String
)
