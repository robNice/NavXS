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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlaySettings
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
            val iconTint = when {
                settings.editMode && !selected -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                else -> Color(button.colorArgb).copy(alpha = button.opacity)
            }
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
                val icon = resolveThemeIcon(de.robnice.navxs.domain.ThemeRegistry().resolve(button.type, button.themeId))
                if (icon != null) {
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

private fun touchTargetDp(iconSizeDp: Int, editMode: Boolean): Int {
    val padding = if (editMode) 24 else 0
    return max(iconSizeDp + padding, 56)
}

private fun fallbackFontSize(iconSizeDp: Int): TextUnit = (iconSizeDp * 0.7f).sp

private fun Modifier.drawButtonOutline(selected: Boolean): Modifier = drawBehind {
    val cornerRadius = 12.dp.toPx()
    val strokeWidth = if (selected) 2.dp.toPx() else 1.5.dp.toPx()
    val outlineColor = if (selected) {
        Color(0xFFB6BCCF)
    } else {
        Color(0xFFB6BCCF).copy(alpha = 0.38f)
    }
    drawRoundRect(
        color = outlineColor,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(
            width = strokeWidth,
            pathEffect = if (selected) {
                null
            } else {
                PathEffect.dashPathEffect(
                    intervals = floatArrayOf(8.dp.toPx(), 6.dp.toPx())
                )
            }
        )
    )
}

private const val TAG = "ButtonPreviewArea"
