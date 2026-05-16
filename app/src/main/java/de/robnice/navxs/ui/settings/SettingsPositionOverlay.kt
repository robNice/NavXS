package de.robnice.navxs.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import de.robnice.navxs.R
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlaySettings
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun SettingsPositionOverlay(
    settings: OverlaySettings,
    precisionOpen: Boolean,
    onSelectButton: (NavButtonType) -> Unit,
    onCommitMoveButtonPosition: (NavButtonType, Int, Int) -> Unit,
    onCloseEditMode: () -> Unit,
    onOpenPrecision: () -> Unit,
    onClosePrecision: () -> Unit,
    onStepChange: (Int) -> Unit,
    onPrecisionMove: (Int, Int) -> Unit,
    onResetPosition: (Map<NavButtonType, Pair<Int, Int>>) -> Unit
) {
    PositioningImmersiveModeEffect()
    val density = LocalDensity.current
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val viewportWidthPx = displayMetrics.widthPixels
    val viewportHeightPx = displayMetrics.heightPixels
    var localSelectedButtonType by remember { mutableStateOf(settings.selectedButtonType) }
    var dragButtonType by remember { mutableStateOf<NavButtonType?>(null) }
    var dragPreviewPosition by remember { mutableStateOf<Offset?>(null) }
    var settlingButtonType by remember { mutableStateOf<NavButtonType?>(null) }
    var settlingPosition by remember { mutableStateOf<Offset?>(null) }
    var localButtonPositions by remember {
        mutableStateOf(
            settings.buttons.mapValues { (_, button) ->
                Offset(button.positionXPx.toFloat(), button.positionYPx.toFloat())
            }
        )
    }
    var isDragging by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        val activeButtonType = dragButtonType ?: localSelectedButtonType
        val activeButton = settings.buttons.getValue(activeButtonType)
        val activeDragBounds = remember(viewportWidthPx, viewportHeightPx, density, activeButton.sizePercent) {
            dragBoundsForButton(
                viewportWidthPx = viewportWidthPx,
                viewportHeightPx = viewportHeightPx,
                sizePercent = activeButton.sizePercent,
                density = density
            )
        }

        fun clampPosition(position: Offset, type: NavButtonType = activeButtonType): Offset {
            val dragBounds = dragBoundsForButton(
                viewportWidthPx = viewportWidthPx,
                viewportHeightPx = viewportHeightPx,
                sizePercent = settings.buttons.getValue(type).sizePercent,
                density = density
            )
            return Offset(
                x = position.x.coerceIn(dragBounds.left.toFloat(), dragBounds.right.toFloat()),
                y = position.y.coerceIn(dragBounds.top.toFloat(), dragBounds.bottom.toFloat())
            )
        }

        LaunchedEffect(settings.buttons, viewportWidthPx, viewportHeightPx, isDragging, dragButtonType) {
            val persistedPositions = settings.buttons.mapValues { (type, button) ->
                clampPosition(Offset(button.positionXPx.toFloat(), button.positionYPx.toFloat()), type)
            }
            persistedPositions.forEach { (type, position) ->
                val button = settings.buttons.getValue(type)
                val dragBounds = dragBoundsForButton(
                    viewportWidthPx = viewportWidthPx,
                    viewportHeightPx = viewportHeightPx,
                    sizePercent = button.sizePercent,
                    density = density
                )
                if (position.x.roundToInt() != button.positionXPx || position.y.roundToInt() != button.positionYPx) {
                    Log.d(
                        TAG,
                        "clampPersisted type=$type from=(${button.positionXPx},${button.positionYPx}) to=$position bounds=$dragBounds"
                    )
                    onCommitMoveButtonPosition(type, position.x.roundToInt(), position.y.roundToInt())
                }
            }
            if (
                settlingButtonType != null &&
                settlingPosition != null &&
                persistedPositions[settlingButtonType] == settlingPosition
            ) {
                settlingButtonType = null
                settlingPosition = null
            }
            if (!isDragging) {
                localButtonPositions = persistedPositions
                dragButtonType = null
                if (settlingButtonType == null) {
                    dragPreviewPosition = null
                }
            }
            localSelectedButtonType = settings.selectedButtonType
        }

        Box(modifier = Modifier.fillMaxSize()) {
            ButtonPreviewArea(
                modifier = Modifier.fillMaxSize(),
                settings = settings,
                showBackground = true,
                buttonPositions = localButtonPositions,
                draggedButtonType = dragButtonType ?: settlingButtonType,
                draggedButtonPosition = dragPreviewPosition ?: settlingPosition,
                selectedButtonTypeOverride = dragButtonType ?: localSelectedButtonType,
                dragBounds = activeDragBounds,
                onSelectButton = {
                    localSelectedButtonType = it
                    onSelectButton(it)
                },
                onDragStarted = { type, startPosition ->
                    dragButtonType = type
                    localSelectedButtonType = type
                    isDragging = true
                    dragPreviewPosition = clampPosition(startPosition, type)
                    onSelectButton(type)
                },
                onMoveSelectedButton = { deltaX, deltaY ->
                    val type = dragButtonType ?: return@ButtonPreviewArea
                    val currentPosition = dragPreviewPosition ?: localButtonPositions[type] ?: return@ButtonPreviewArea
                    val nextPosition = clampPosition(
                        Offset(
                            x = currentPosition.x + deltaX,
                            y = currentPosition.y + deltaY,
                        ),
                        type
                    )
                    if (nextPosition != currentPosition) {
                        dragPreviewPosition = nextPosition
                    }
                },
                onDragFinished = {
                    val commitType = dragButtonType ?: localSelectedButtonType
                    val persistedButton = settings.buttons.getValue(commitType)
                    val localPosition = dragPreviewPosition ?: localButtonPositions.getValue(commitType)
                    Log.d(
                        TAG,
                        "commitDrag type=$commitType target=$localPosition bounds=$activeDragBounds"
                    )
                    localButtonPositions = localButtonPositions + (commitType to localPosition)
                    settlingButtonType = commitType
                    settlingPosition = localPosition
                    dragPreviewPosition = null
                    if (localPosition.x.roundToInt() != persistedButton.positionXPx || localPosition.y.roundToInt() != persistedButton.positionYPx) {
                        onCommitMoveButtonPosition(commitType, localPosition.x.roundToInt(), localPosition.y.roundToInt())
                    }
                    isDragging = false
                }
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_drag_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(onClick = onCloseEditMode) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.settings_done)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = {
                            val clampedPositions = navBarResetPositions(
                                settings = settings,
                                viewportSize = IntSize(viewportWidthPx, viewportHeightPx),
                                density = density
                            ).mapValues { (type, position) ->
                                clampPosition(position, type)
                            }
                            settlingButtonType = null
                            settlingPosition = null
                            localButtonPositions = localButtonPositions + clampedPositions
                            onResetPosition(
                                clampedPositions.mapValues { (_, position) ->
                                    position.x.roundToInt() to position.y.roundToInt()
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = stringResource(R.string.settings_reset_position)
                        )
                    }
                    if (!precisionOpen) {
                        FilledTonalIconButton(onClick = onOpenPrecision) {
                            Icon(
                                imageVector = Icons.Outlined.OpenWith,
                                contentDescription = stringResource(R.string.settings_open_precision)
                            )
                        }
                    }
                }
            }
            if (precisionOpen) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    PrecisionControls(
                        settings = settings,
                        onDismiss = onClosePrecision,
                        onStepChange = onStepChange,
                        onMove = onPrecisionMove,
                        onResetPosition = {
                            val clampedPositions = navBarResetPositions(
                                settings = settings,
                                viewportSize = IntSize(viewportWidthPx, viewportHeightPx),
                                density = density
                            ).mapValues { (type, position) ->
                                clampPosition(position, type)
                            }
                            settlingButtonType = null
                            settlingPosition = null
                            localButtonPositions = localButtonPositions + clampedPositions
                            onResetPosition(
                                clampedPositions.mapValues { (_, position) ->
                                    position.x.roundToInt() to position.y.roundToInt()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun dragBoundsForButton(
    viewportWidthPx: Int,
    viewportHeightPx: Int,
    sizePercent: Int,
    density: androidx.compose.ui.unit.Density
): IntRect {
    val iconPx = with(density) { iconSizeDp(sizePercent).dp.roundToPx() }
    val touchTargetPx = with(density) { max(iconSizeDp(sizePercent) + 24, 56).dp.roundToPx() }
    val overflowPx = max(touchTargetPx - iconPx, 0) / 2
    val bottomAllowancePx = iconPx / 2
    return IntRect(
        left = -overflowPx,
        top = -overflowPx,
        right = max(viewportWidthPx - iconPx - overflowPx, -overflowPx),
        bottom = max(viewportHeightPx - iconPx + bottomAllowancePx, -overflowPx)
    )
}

private fun navBarResetPositions(
    settings: OverlaySettings,
    viewportSize: IntSize,
    density: androidx.compose.ui.unit.Density
): Map<NavButtonType, Offset> {
    val metrics = android.util.DisplayMetrics().apply {
        widthPixels = viewportSize.width
        heightPixels = viewportSize.height
        this.density = density.density
    }
    val positions = NavDefaults.defaultButtonPositions(
        displayMetrics = metrics,
        sizePercentByType = settings.buttons.mapValues { it.value.sizePercent }
    )
    return settings.buttons.mapValues { (type, _) ->
        val (x, y) = positions.getValue(type)
        Offset(x.toFloat(), y.toFloat())
    }
}

@Composable
private fun PositioningImmersiveModeEffect() {
    val view = LocalView.current
    val activity = remember(view) { view.context.findActivity() }

    DisposableEffect(activity, view) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            val previousBehavior = controller.systemBarsBehavior
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())

            onDispose {
                controller.systemBarsBehavior = previousBehavior
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        } else {
            onDispose {}
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private const val TAG = "PositionOverlay"
