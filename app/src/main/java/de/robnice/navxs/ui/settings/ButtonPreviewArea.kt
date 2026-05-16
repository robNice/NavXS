package de.robnice.navxs.ui.settings

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
import de.robnice.navxs.data.models.OverlaySettings
import de.robnice.navxs.domain.ThemeRegistry
import de.robnice.navxs.ui.theme.themeDrawableRes
import kotlin.math.max

@Composable
fun ButtonPreviewArea(
    modifier: Modifier = Modifier,
    settings: OverlaySettings,
    showBackground: Boolean,
    buttonPositions: Map<NavButtonType, Offset> = emptyMap(),
    draggedButtonType: NavButtonType? = null,
    draggedButtonPosition: Offset? = null,
    selectedButtonTypeOverride: NavButtonType? = null,
    dragBounds: IntRect? = null,
    onSelectButton: (NavButtonType) -> Unit = {},
    onDragStarted: (NavButtonType, Offset) -> Unit = { _, _ -> },
    onMoveSelectedButton: (Float, Float) -> Unit,
    onDragFinished: () -> Unit = {}
) {
    val density = LocalDensity.current
    val effectiveSelectedType = selectedButtonTypeOverride ?: settings.selectedButtonType
    val currentButtonPositions by rememberUpdatedState(buttonPositions)
    val currentDraggedButtonType by rememberUpdatedState(draggedButtonType)
    val currentDraggedButtonPosition by rememberUpdatedState(draggedButtonPosition)
    val currentDragBounds by rememberUpdatedState(dragBounds)
    val currentSettings by rememberUpdatedState(settings)
    val currentOnSelectButton by rememberUpdatedState(onSelectButton)
    val currentOnDragStarted by rememberUpdatedState(onDragStarted)
    val currentOnMoveSelectedButton by rememberUpdatedState(onMoveSelectedButton)
    val currentOnDragFinished by rememberUpdatedState(onDragFinished)
    var dragActive by remember(settings.editMode) {
        mutableStateOf(false)
    }
    var dragType by remember(settings.editMode) { mutableStateOf<NavButtonType?>(null) }
    var pressedType by remember(settings.editMode) { mutableStateOf<NavButtonType?>(null) }

    fun buttonPosition(type: NavButtonType): Offset {
        if (type == currentDraggedButtonType && currentDraggedButtonPosition != null) {
            return currentDraggedButtonPosition!!
        }
        return currentButtonPositions[type]
            ?: settings.buttons[type]?.let { Offset(it.positionXPx.toFloat(), it.positionYPx.toFloat()) }
            ?: Offset.Zero
    }

    fun hitButtonAt(offset: Offset) = currentSettings.buttons.values
        .filter { it.active }
        .lastOrNull { button ->
            val iconSizeDp = iconSizeDp(button.sizePercent)
            val touchTargetDp = touchTargetDp(iconSizeDp, currentSettings.editMode)
            val topLeft = buttonPosition(button.type)
            Rect(
                offset = topLeft,
                size = Size(
                    width = with(density) { touchTargetDp.dp.toPx() },
                    height = with(density) { touchTargetDp.dp.toPx() }
                )
            ).contains(offset)
        }

    Box(
        modifier = modifier
            .background(
                color = if (showBackground) MaterialTheme.colorScheme.surface.copy(alpha = 0.98f) else Color.Transparent,
                shape = RoundedCornerShape(if (showBackground) 0.dp else 24.dp)
            )
            .pointerInput(settings.editMode) {
                if (settings.editMode) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        pressedType = hitButtonAt(down.position)?.type
                        waitForUpOrCancellation()
                        if (!dragActive) {
                            pressedType = null
                        }
                    }
                }
            }
            .pointerInput(settings.editMode) {
                if (settings.editMode) {
                    detectTapGestures(
                        onTap = { offset ->
                            hitButtonAt(offset)?.let { button ->
                                currentOnSelectButton(button.type)
                            }
                        }
                    )
                }
            }
            .pointerInput(settings.editMode) {
                if (settings.editMode) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val hitButton = pressedType?.let { type ->
                                currentSettings.buttons[type]
                            } ?: hitButtonAt(offset)
                            dragType = hitButton?.type
                            dragActive = hitButton != null
                            if (hitButton != null) {
                                if (hitButton.type != currentSettings.selectedButtonType) {
                                    currentOnSelectButton(hitButton.type)
                                }
                                currentOnDragStarted(
                                    hitButton.type,
                                    buttonPosition(hitButton.type)
                                )
                            }
                            Log.d(
                                TAG,
                                "dragStart type=${hitButton?.type} hit=$dragActive start=$offset current=${dragType?.let(::buttonPosition)} bounds=$currentDragBounds"
                            )
                        },
                        onDragCancel = {
                            dragActive = false
                            dragType = null
                            pressedType = null
                            Log.d(TAG, "dragCancel type=${settings.selectedButtonType}")
                            currentOnDragFinished()
                        },
                        onDragEnd = {
                            dragActive = false
                            Log.d(TAG, "dragEnd type=$dragType final=${dragType?.let(::buttonPosition)}")
                            dragType = null
                            pressedType = null
                            currentOnDragFinished()
                        }
                    ) { change, dragAmount ->
                        if (dragActive) {
                            change.consume()
                            currentOnMoveSelectedButton(dragAmount.x, dragAmount.y)
                        }
                    }
                }
            }
            .testTag("button_preview_area")
    ) {
        if (showBackground) {
            DottedBackdrop()
        }
        settings.buttons.values.filter { it.active }.forEach { button ->
            val selected = button.type == effectiveSelectedType
            val iconSizeDp = iconSizeDp(button.sizePercent)
            val touchTargetDp = touchTargetDp(iconSizeDp, settings.editMode)
            val iconTint = Color(button.colorArgb).copy(alpha = button.opacity)
            val position = buttonPosition(button.type)
            val buttonOffset = IntOffset(position.x.toInt(), position.y.toInt())
            Box(
                modifier = Modifier
                    .offset { buttonOffset }
                    .size(touchTargetDp.dp)
                    .then(
                        if (settings.editMode) {
                            Modifier.drawButtonOutline(selected)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                ButtonBackground(
                    colorArgb = button.backgroundColorArgb,
                    opacity = button.backgroundOpacity,
                    sizePercent = button.backgroundSizePercent,
                    softnessPercent = button.backgroundSoftnessPercent,
                    iconSizeDp = iconSizeDp,
                    enabled = true
                )
                val theme = ThemeRegistry().resolve(button.type, button.themeId)
                val drawableRes = themeDrawableRes(theme.vectorAssetName)
                val icon = resolveThemeIcon(theme)
                if (drawableRes != null) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = button.type.name,
                        modifier = Modifier.size(iconSizeDp.dp),
                        colorFilter = ColorFilter.tint(iconTint)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = button.type.name,
                        modifier = Modifier.size(iconSizeDp.dp),
                        tint = iconTint
                    )
                } else {
                    Text(
                        text = fallbackLabel(button.type),
                        color = iconTint,
                        fontSize = fallbackFontSize(iconSizeDp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonBackground(
    colorArgb: Long,
    opacity: Float,
    sizePercent: Int,
    softnessPercent: Int,
    iconSizeDp: Int,
    enabled: Boolean
) {
    if (!enabled || opacity <= 0f) return
    val backgroundSizeDp = backgroundSizeDp(iconSizeDp, sizePercent)
    val color = Color(colorArgb).copy(alpha = opacity)
    val softness = (softnessPercent.coerceIn(0, 100) / 100f)
    if (softness <= 0f) {
        Box(
            modifier = Modifier
                .size(backgroundSizeDp.dp)
                .background(color = color, shape = CircleShape)
        )
    } else {
        val innerStop = (1f - softness).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .size(backgroundSizeDp.dp)
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to color,
                            innerStop to color,
                            1f to color.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun DottedBackdrop() {
    val dotColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 28.dp.toPx()
        val radius = 1.1.dp.toPx()
        var y = spacing / 2
        while (y < size.height) {
            var x = spacing / 2
            while (x < size.width) {
                drawCircle(dotColor, radius, Offset(x, y))
                x += spacing
            }
            y += spacing
        }
    }
}

private fun fallbackLabel(type: NavButtonType): String = when (type) {
    NavButtonType.BACK -> "\u2039"
    NavButtonType.HOME -> "\u25CB"
    NavButtonType.RECENTS -> "\u25A2"
}

internal fun iconSizeDp(sizePercent: Int): Int = max((32 * sizePercent) / 100, 16)

internal fun backgroundSizeDp(iconSizeDp: Int, sizePercent: Int): Int =
    max((iconSizeDp * sizePercent) / 100, 16)

private fun touchTargetDp(iconSizeDp: Int, editMode: Boolean): Int {
    val padding = if (editMode) 24 else 0
    return max(iconSizeDp + padding, 56)
}

private fun fallbackFontSize(iconSizeDp: Int): TextUnit = (iconSizeDp * 0.7f).sp

private fun Modifier.drawButtonOutline(selected: Boolean): Modifier = drawBehind {
    val radius = minOf(size.width, size.height) / 2f
    if (selected) {
        val strokeWidth = 3.dp.toPx()
        drawCircle(
            color = Color(0xFFFF69B4).copy(alpha = 0.15f),
            radius = radius - strokeWidth / 2f
        )
        drawCircle(
            color = Color(0xFFFF69B4),
            radius = radius - strokeWidth / 2f,
            style = Stroke(width = strokeWidth)
        )
    } else {
        val strokeWidth = 1.5.dp.toPx()
        drawCircle(
            color = Color(0xFFB6BCCF).copy(alpha = 0.38f),
            radius = radius - strokeWidth / 2f,
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(8.dp.toPx(), 6.dp.toPx())
                )
            )
        )
    }
}

@Composable
internal fun SingleButtonPreview(
    config: OverlayButtonConfig,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val iconSizeDp = iconSizeDp(config.sizePercent)
    val iconTint = Color(config.colorArgb).copy(alpha = config.opacity)
    val theme = remember(config.type, config.themeId) {
        ThemeRegistry().resolve(config.type, config.themeId)
    }
    val drawableRes = themeDrawableRes(theme.vectorAssetName)
    val icon = resolveThemeIcon(theme)
    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        ButtonBackground(
            colorArgb = config.backgroundColorArgb,
            opacity = config.backgroundOpacity,
            sizePercent = config.backgroundSizePercent,
            softnessPercent = config.backgroundSoftnessPercent,
            iconSizeDp = iconSizeDp,
            enabled = true
        )
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp.dp),
                colorFilter = ColorFilter.tint(iconTint)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSizeDp.dp),
                tint = iconTint
            )
        } else {
            Text(
                text = fallbackLabel(config.type),
                color = iconTint,
                fontSize = fallbackFontSize(iconSizeDp),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private const val TAG = "ButtonPreviewArea"
