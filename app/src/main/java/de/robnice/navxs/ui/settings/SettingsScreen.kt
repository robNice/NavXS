package de.robnice.navxs.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import de.robnice.navxs.R
import de.robnice.navxs.data.models.InstalledAppInfo
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
import de.robnice.navxs.domain.ThemeRegistry
import de.robnice.navxs.ui.MainUiState
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    state: MainUiState,
    onSelectConfiguration: (String?) -> Unit,
    onIndividualConfigurationChange: (Boolean) -> Unit,
    onCopyConfiguration: (String?) -> Unit,
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
    val selectedPackage = state.selectedConfigurationPackage
    val selectedApp = state.installedApps.firstOrNull { it.packageName == selectedPackage }
    val activeApps = state.installedApps
        .filter { it.enabled }
        .sortedBy { it.appName.lowercase() }
    val individualEnabled = selectedPackage == null ||
        state.appConfigurations[selectedPackage]?.individualEnabled == true
    val controlsEnabled = individualEnabled
    val selectedType = state.settings.selectedButtonType
    val selectedButton = state.settings.buttons.getValue(selectedType)
    val buttonDetailsEnabled = controlsEnabled && selectedButton.active
    val themes = ThemeRegistry().themesFor(selectedType)
    val copySources = buildList {
        if (selectedPackage != null) {
            add(
                CopySource(
                    id = DefaultSourceId,
                    packageName = null,
                    app = null
                )
            )
        }
        activeApps
            .filter { app ->
                app.packageName != selectedPackage &&
                    state.appConfigurations[app.packageName]?.individualEnabled == true
            }
            .forEach { app ->
                add(
                    CopySource(
                        id = app.packageName,
                        packageName = app.packageName,
                        app = app
                    )
                )
            }
    }
    var previewOpen by remember { mutableStateOf(false) }
    var copyDialogOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ConfigurationSelector(
                            selectedApp = selectedApp,
                            activeApps = activeApps,
                            onSelectConfiguration = onSelectConfiguration
                        )
                        if (controlsEnabled) {
                            OutlinedButton(
                                onClick = { copyDialogOpen = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = null
                                )
                                Text(
                                    text = stringResource(R.string.settings_copy_from),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedPackage != null) {
                item {
                    SettingsCard {
                        SettingHeader(
                            title = stringResource(R.string.settings_individual_design_layout),
                            subtitle = stringResource(R.string.settings_individual_design_layout_description)
                        ) {
                            Switch(
                                checked = individualEnabled,
                                onCheckedChange = onIndividualConfigurationChange
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(modifier = Modifier.alpha(if (controlsEnabled) 1f else DisabledAlpha)) {
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
                            enabled = controlsEnabled,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                            )
                        ) {
                            Icon(imageVector = Icons.Outlined.OpenWith, contentDescription = null)
                            Text(
                                text = stringResource(R.string.settings_position_button),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(modifier = Modifier.alpha(if (controlsEnabled) 1f else DisabledAlpha)) {
                    Text(
                        text = stringResource(R.string.settings_select_button),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ButtonSelector(
                        selectedType = selectedType,
                        enabled = controlsEnabled,
                        onSelected = onSelectButton
                    )
                }
            }

            item {
                SettingsCard(modifier = Modifier.alpha(if (controlsEnabled) 1f else DisabledAlpha)) {
                    SettingHeader(
                        title = stringResource(R.string.settings_active),
                        subtitle = stringResource(R.string.settings_active_description)
                    ) {
                        Switch(
                            checked = selectedButton.active,
                            enabled = controlsEnabled,
                            onCheckedChange = { onActiveChange(selectedType, it) }
                        )
                    }
                }
            }

            item {
                SettingsCard(modifier = Modifier.alpha(if (buttonDetailsEnabled) 1f else DisabledAlpha)) {
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
                        enabled = buttonDetailsEnabled,
                        onThemeSelected = { onThemeChange(selectedType, it) }
                    )
                    SettingsDivider()
                    SettingHeader(
                        title = stringResource(R.string.settings_colour),
                        subtitle = stringResource(R.string.settings_colour_description)
                    ) {
                        ColorPicker(
                            selectedColor = selectedButton.colorArgb,
                            enabled = buttonDetailsEnabled,
                            onSelected = { onColorChange(selectedType, it) }
                        )
                    }
                    SettingsDivider()
                    SliderSetting(
                        label = stringResource(R.string.settings_opacity),
                        subtitle = stringResource(R.string.settings_opacity_description),
                        value = selectedButton.opacity,
                        valueText = stringResource(
                            R.string.percent_format,
                            (selectedButton.opacity * 100).toInt()
                        ),
                        enabled = buttonDetailsEnabled,
                        onValueChange = { onOpacityChange(selectedType, it) }
                    )
                    SettingsDivider()
                    SliderSetting(
                        label = stringResource(R.string.settings_size),
                        subtitle = stringResource(R.string.settings_size_description),
                        value = ((selectedButton.sizePercent - 100) / 200f).coerceIn(0f, 1f),
                        valueText = stringResource(R.string.percent_format, selectedButton.sizePercent),
                        enabled = buttonDetailsEnabled,
                        onValueChange = { onSizeChange(selectedType, (100 + it * 200).toInt()) }
                    )
                }
            }

            item {
                SettingsCard(modifier = Modifier.alpha(if (buttonDetailsEnabled) 1f else DisabledAlpha)) {
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
                            enabled = buttonDetailsEnabled,
                            onSelected = { onBackgroundColorChange(selectedType, it) }
                        )
                    }
                    SettingsDivider()
                    SliderSetting(
                        label = stringResource(R.string.settings_button_background_opacity),
                        subtitle = stringResource(R.string.settings_button_background_opacity_description),
                        value = selectedButton.backgroundOpacity,
                        valueText = stringResource(
                            R.string.percent_format,
                            (selectedButton.backgroundOpacity * 100).toInt()
                        ),
                        enabled = buttonDetailsEnabled,
                        onValueChange = { onBackgroundOpacityChange(selectedType, it) }
                    )
                    SettingsDivider()
                    SliderSetting(
                        label = stringResource(R.string.settings_button_background_size),
                        subtitle = stringResource(R.string.settings_button_background_size_description),
                        value = ((selectedButton.backgroundSizePercent - 100) / 200f).coerceIn(0f, 1f),
                        valueText = stringResource(
                            R.string.percent_format,
                            selectedButton.backgroundSizePercent
                        ),
                        enabled = buttonDetailsEnabled,
                        onValueChange = {
                            onBackgroundSizeChange(selectedType, (100 + it * 200).toInt())
                        }
                    )
                    SettingsDivider()
                    SliderSetting(
                        label = stringResource(R.string.settings_button_background_softness),
                        subtitle = stringResource(R.string.settings_button_background_softness_description),
                        value = selectedButton.backgroundSoftnessPercent / 100f,
                        valueText = stringResource(
                            R.string.percent_format,
                            selectedButton.backgroundSoftnessPercent
                        ),
                        enabled = buttonDetailsEnabled,
                        onValueChange = {
                            onBackgroundSoftnessChange(selectedType, (it * 100).toInt())
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = previewOpen,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(220)),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(180)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp)
        ) {
            PreviewOffCanvas(config = selectedButton)
        }
        PreviewFlag(
            isOpen = previewOpen,
            onClick = { previewOpen = !previewOpen },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp)
        )
    }

    if (copyDialogOpen) {
        CopyConfigurationDialog(
            sources = copySources,
            onDismiss = { copyDialogOpen = false },
            onConfirm = { source ->
                onCopyConfiguration(source.packageName)
                copyDialogOpen = false
            }
        )
    }
}

@Composable
private fun ConfigurationSelector(
    selectedApp: InstalledAppInfo?,
    activeApps: List<InstalledAppInfo>,
    onSelectConfiguration: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val defaultLabel = stringResource(R.string.settings_default_layout)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.settings_configuration_for),
            style = MaterialTheme.typography.labelLarge
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                ConfigurationIcon(app = selectedApp, contentDescription = null)
                Text(
                    text = selectedApp?.appName ?: defaultLabel,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                    textAlign = TextAlign.Start,
                    maxLines = 1
                )
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(defaultLabel) },
                    leadingIcon = {
                        ConfigurationIcon(app = null, contentDescription = null)
                    },
                    onClick = {
                        expanded = false
                        onSelectConfiguration(null)
                    }
                )
                activeApps.forEach { app ->
                    DropdownMenuItem(
                        text = { Text(app.appName) },
                        leadingIcon = {
                            ConfigurationIcon(app = app, contentDescription = null)
                        },
                        onClick = {
                            expanded = false
                            onSelectConfiguration(app.packageName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CopyConfigurationDialog(
    sources: List<CopySource>,
    onDismiss: () -> Unit,
    onConfirm: (CopySource) -> Unit
) {
    var selectedSourceId by remember(sources) { mutableStateOf<String?>(null) }
    val selectedSource = sources.firstOrNull { it.id == selectedSourceId }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_copy_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.settings_copy_dialog_warning),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (sources.isEmpty()) {
                    Text(
                        text = stringResource(R.string.settings_copy_dialog_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(sources, key = { it.id }) { source ->
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSourceId = source.id },
                                headlineContent = {
                                    Text(
                                        source.app?.appName
                                            ?: stringResource(R.string.settings_default_layout)
                                    )
                                },
                                supportingContent = source.app?.let { app ->
                                    { Text(app.packageName) }
                                },
                                leadingContent = {
                                    ConfigurationIcon(app = source.app, contentDescription = null)
                                },
                                trailingContent = {
                                    RadioButton(
                                        selected = selectedSourceId == source.id,
                                        onClick = { selectedSourceId = source.id }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedSource != null,
                onClick = { selectedSource?.let(onConfirm) }
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
internal fun ConfigurationIcon(
    app: InstalledAppInfo?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(app?.packageName, app?.icon) {
        runCatching { app?.icon?.toBitmap()?.asImageBitmap() }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier.size(28.dp)
        )
    } else {
        Icon(
            imageVector = Icons.Outlined.Layers,
            contentDescription = contentDescription,
            modifier = modifier.size(28.dp)
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
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
internal fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
internal fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
}

@Composable
internal fun SettingHeader(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier
                .height(HeaderAccessoryHeight)
                .padding(start = 12.dp),
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
                val radius = 12.dp.toPx()
                val path = Path().apply {
                    moveTo(size.width, 0f)
                    lineTo(radius, 0f)
                    quadraticTo(0f, 0f, 0f, radius)
                    lineTo(0f, size.height - radius)
                    quadraticTo(0f, size.height, radius, size.height)
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
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                SingleButtonPreview(
                    config = config,
                    backgroundColor = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                SingleButtonPreview(
                    config = config,
                    backgroundColor = Color.Black,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

private data class CopySource(
    val id: String,
    val packageName: String?,
    val app: InstalledAppInfo?
)

private const val DefaultSourceId = "__default_layout__"
private const val DisabledAlpha = 0.45f
private val HeaderAccessoryHeight = 28.dp
