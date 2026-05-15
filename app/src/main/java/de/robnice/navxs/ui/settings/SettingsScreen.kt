package de.robnice.navxs.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.robnice.navxs.R
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.domain.ThemeRegistry
import de.robnice.navxs.ui.MainUiState

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    state: MainUiState,
    onSelectButton: (NavButtonType) -> Unit,
    onActiveChange: (NavButtonType, Boolean) -> Unit,
    onColorChange: (NavButtonType, Long) -> Unit,
    onOpacityChange: (NavButtonType, Float) -> Unit,
    onSizeChange: (NavButtonType, Int) -> Unit,
    onThemeChange: (NavButtonType, String) -> Unit,
    onOpenEditMode: () -> Unit
) {
    val selectedType = state.settings.selectedButtonType
    val selectedButton = state.settings.buttons.getValue(selectedType)
    val controlsEnabled = selectedButton.active
    val themes = ThemeRegistry().themesFor(selectedType)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.settings_edit_mode),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.settings_drag_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenEditMode,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                        )
                    ) {
                        Text(stringResource(R.string.settings_position_button))
                    }
                }
            }
        }
        item {
            SettingsCard {
                Text(
                    text = stringResource(R.string.settings_select_button),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ButtonSelector(selectedType = selectedType, onSelected = onSelectButton)
            }
        }
        item {
            SettingsCard {
                SettingHeader(
                    title = stringResource(R.string.settings_active),
                    subtitle = stringResource(R.string.settings_active_description)
                ) {
                    Switch(
                        checked = selectedButton.active,
                        onCheckedChange = { onActiveChange(selectedType, it) }
                    )
                }
                SettingsDivider()
                SettingHeader(
                    title = stringResource(R.string.settings_colour),
                    subtitle = stringResource(R.string.settings_colour_description)
                ) {
                    ColorPicker(
                        selectedColor = selectedButton.colorArgb,
                        enabled = controlsEnabled,
                        onSelected = { onColorChange(selectedType, it) }
                    )
                }
                SettingsDivider()
                SliderSetting(
                    label = stringResource(R.string.settings_opacity),
                    subtitle = stringResource(R.string.settings_opacity_description),
                    value = selectedButton.opacity,
                    valueText = stringResource(R.string.percent_format, (selectedButton.opacity * 100).toInt()),
                    enabled = controlsEnabled,
                    onValueChange = { onOpacityChange(selectedType, it) }
                )
                SettingsDivider()
                SliderSetting(
                    label = stringResource(R.string.settings_size),
                    subtitle = stringResource(R.string.settings_size_description),
                    value = (selectedButton.sizePercent - 100) / 200f,
                    valueText = stringResource(R.string.percent_format, selectedButton.sizePercent),
                    enabled = controlsEnabled,
                    onValueChange = { onSizeChange(selectedType, (100 + it * 200).toInt()) }
                )
                SettingsDivider()
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.settings_theme),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.settings_theme_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ThemePicker(
                        themes = themes,
                        selectedThemeId = selectedButton.themeId,
                        enabled = controlsEnabled,
                        onThemeSelected = { onThemeChange(selectedType, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SliderSetting(
    label: String,
    subtitle: String,
    value: Float,
    valueText: String,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.height(HeaderAccessoryHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Slider(
            value = value,
            enabled = enabled,
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
}

@Composable
private fun SettingHeader(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.height(HeaderAccessoryHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            trailing()
        }
    }
}

private val HeaderAccessoryHeight = 28.dp
