package de.robnice.navxs.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.robnice.navxs.R
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
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
    onBackgroundColorChange: (NavButtonType, Long) -> Unit,
    onBackgroundOpacityChange: (NavButtonType, Float) -> Unit,
    onBackgroundSizeChange: (NavButtonType, Int) -> Unit,
    onBackgroundSoftnessChange: (NavButtonType, Int) -> Unit,
    onThemeChange: (NavButtonType, String) -> Unit,
    onOpenEditMode: () -> Unit
) {
    val selectedType = state.settings.selectedButtonType
    val selectedButton = state.settings.buttons.getValue(selectedType)
    val controlsEnabled = selectedButton.active
    val themes = ThemeRegistry().themesFor(selectedType)
    var previewOpen by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(end = 16.dp),
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
            }
        }
        if (controlsEnabled) {
            item {
                SettingsCard {
                    Text(
                        text = stringResource(R.string.settings_button_section),
                        style = MaterialTheme.typography.titleMedium
                    )
                    SettingsDivider()
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
                        enabled = true,
                        onThemeSelected = { onThemeChange(selectedType, it) }
                    )
                    SettingsDivider()
                    SettingHeader(
                        title = stringResource(R.string.settings_colour),
                        subtitle = stringResource(R.string.settings_colour_description)
                    ) {
                        ColorPicker(
                            selectedColor = selectedButton.colorArgb,
                            enabled = true,
                            onSelected = { onColorChange(selectedType, it) }
                        )
                    }
                    SettingsDivider()
                    SliderSetting(
                        label = stringResource(R.string.settings_opacity),
                        subtitle = stringResource(R.string.settings_opacity_description),
                        value = selectedButton.opacity,
                        valueText = stringResource(R.string.percent_format, (selectedButton.opacity * 100).toInt()),
                        enabled = true,
                        onValueChange = { onOpacityChange(selectedType, it) }
                    )
                    SettingsDivider()
                    SliderSetting(
                        label = stringResource(R.string.settings_size),
                        subtitle = stringResource(R.string.settings_size_description),
                        value = (selectedButton.sizePercent - 100) / 200f,
                        valueText = stringResource(R.string.percent_format, selectedButton.sizePercent),
                        enabled = true,
                        onValueChange = { onSizeChange(selectedType, (100 + it * 200).toInt()) }
                    )
                }
            }
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.settings_button_background_section),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.settings_button_background_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SettingsDivider()
                        SettingHeader(
                            title = stringResource(R.string.settings_button_background_colour),
                            subtitle = stringResource(R.string.settings_button_background_colour_description)
                        ) {
                            ColorPicker(
                                selectedColor = selectedButton.backgroundColorArgb,
                                enabled = true,
                                onSelected = { onBackgroundColorChange(selectedType, it) }
                            )
                        }
                        SettingsDivider()
                        SliderSetting(
                            label = stringResource(R.string.settings_button_background_opacity),
                            subtitle = stringResource(R.string.settings_button_background_opacity_description),
                            value = selectedButton.backgroundOpacity,
                            valueText = stringResource(R.string.percent_format, (selectedButton.backgroundOpacity * 100).toInt()),
                            enabled = true,
                            onValueChange = { onBackgroundOpacityChange(selectedType, it) }
                        )
                        SettingsDivider()
                        SliderSetting(
                        label = stringResource(R.string.settings_button_background_size),
                        subtitle = stringResource(R.string.settings_button_background_size_description),
                        value = (selectedButton.backgroundSizePercent - 100) / 200f,
                        valueText = stringResource(R.string.percent_format, selectedButton.backgroundSizePercent),
                        enabled = true,
                        onValueChange = { onBackgroundSizeChange(selectedType, (100 + it * 200).toInt()) }
                    )
                    SettingsDivider()
                    SliderSetting(
                        label = stringResource(R.string.settings_button_background_softness),
                        subtitle = stringResource(R.string.settings_button_background_softness_description),
                        value = selectedButton.backgroundSoftnessPercent / 100f,
                        valueText = stringResource(R.string.percent_format, selectedButton.backgroundSoftnessPercent),
                        enabled = true,
                        onValueChange = { onBackgroundSoftnessChange(selectedType, (it * 100).toInt()) }
                    )
                }
            }
        }
        }
    }
    AnimatedVisibility(
        visible = previewOpen,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(220)),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(180)),
        modifier = Modifier.align(Alignment.TopEnd).padding(top = 56.dp)
    ) {
        PreviewOffCanvas(config = selectedButton)
    }
    PreviewFlag(
        isOpen = previewOpen,
        onClick = { previewOpen = !previewOpen },
        modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp)
    )
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
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
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

@Composable
private fun PreviewFlag(
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(isOpen) {
        if (!isOpen) {
            delay(3000)
            while (true) {
                isBlinking = true
                delay(130)
                isBlinking = false
                delay(4500)
            }
        } else {
            isBlinking = false
        }
    }
    val blinkScale by animateFloatAsState(
        targetValue = if (isBlinking) 0.05f else 1f,
        animationSpec = tween(durationMillis = if (isBlinking) 60 else 90),
        label = "blink"
    )
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
    Box(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape, clip = false)
            .drawBehind {
                val stroke = 1.dp.toPx()
                val r = 12.dp.toPx()
                val path = Path().apply {
                    moveTo(size.width, 0f)
                    lineTo(r, 0f)
                    quadraticTo(0f, 0f, 0f, r)
                    lineTo(0f, size.height - r)
                    quadraticTo(0f, size.height, r, size.height)
                    lineTo(size.width, size.height)
                }
                drawPath(path, outlineColor, style = Stroke(width = stroke))
            }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isOpen) Icons.Rounded.Close else Icons.Outlined.Visibility,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer { scaleY = blinkScale }
        )
    }
}

@Composable
private fun PreviewOffCanvas(
    config: OverlayButtonConfig,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_preview_title),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SingleButtonPreview(
                    config = config,
                    backgroundColor = Color.White,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                )
                SingleButtonPreview(
                    config = config,
                    backgroundColor = Color(0xFF9E9E9E),
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                )
                SingleButtonPreview(
                    config = config,
                    backgroundColor = Color.Black,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

private val HeaderAccessoryHeight = 28.dp
