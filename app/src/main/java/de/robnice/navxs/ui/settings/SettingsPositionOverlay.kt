package de.robnice.navxs.ui.settings

import android.app.Activity
import android.content.ContextWrapper
import android.util.Log
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
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
    onPrecisionMove: (NavButtonType, Int, Int) -> Unit,
    onResetPosition: (Map<NavButtonType, Pair<Int, Int>>) -> Unit
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val viewportWidthPx = displayMetrics.widthPixels
    val viewportHeightPx = displayMetrics.heightPixels
    val rawNavBarBottomPx = WindowInsets.navigationBars.getBottom(density)
    val stableNavBarBottomPx = remember { rawNavBarBottomPx }
    val view = LocalView.current
    DisposableEffect(view) {
        var ctx: android.content.Context? = view.context
        while (ctx is ContextWrapper && ctx !is Activity) ctx = ctx.baseContext
        val window = (ctx as? Activity)?.window
        if (window != null) {
            val controller = WindowInsetsControllerCompat(window, view)
            val prevBehavior = controller.systemBarsBehavior
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            onDispose {
                controller.systemBarsBehavior = prevBehavior
                controller.show(WindowInsetsCompat.Type.navigationBars())
            }
        } else {
            onDispose {}
        }
    }
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

        LaunchedEffect(settings.buttons, viewportWidthPx, viewportHeightPx, isDragging, dragButtonType, precisionOpen) {
            val persistedPositions = settings.buttons.mapValues { (type, button) ->
                clampPosition(Offset(button.positionXPx.toFloat(), button.positionYPx.toFloat()), type)
            }
            if (
                settlingButtonType != null &&
                settlingPosition != null &&
                persistedPositions[settlingButtonType] == settlingPosition
            ) {
                settlingButtonType = null
                settlingPosition = null
            }
            if (!isDragging && !precisionOpen) {
                localButtonPositions = persistedPositions
                localSelectedButtonType = settings.selectedButtonType
                dragButtonType = null
                if (settlingButtonType == null) {
                    dragPreviewPosition = null
                }
            }
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
                    .statusBarsPadding()
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
                                navBarBottomPx = stableNavBarBottomPx,
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
                        onMove = { dx, dy ->
                            val type = localSelectedButtonType
                            val currentPos = localButtonPositions[type] ?: return@PrecisionControls
                            val newPos = clampPosition(
                                Offset(currentPos.x + dx, currentPos.y + dy),
                                type
                            )
                            localButtonPositions = localButtonPositions + (type to newPos)
                            onPrecisionMove(type, newPos.x.roundToInt(), newPos.y.roundToInt())
                        },
                        onResetPosition = {
                            val clampedPositions = navBarResetPositions(
                                settings = settings,
                                viewportSize = IntSize(viewportWidthPx, viewportHeightPx),
                                navBarBottomPx = stableNavBarBottomPx,
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
    val halfIconPx = iconPx / 2
    return IntRect(
        left = -overflowPx,
        top = -overflowPx,
        right = max(viewportWidthPx - iconPx - overflowPx, -overflowPx),
        bottom = max(viewportHeightPx - iconPx + halfIconPx, -overflowPx)
    )
}

private fun navBarResetPositions(
    settings: OverlaySettings,
    viewportSize: IntSize,
    navBarBottomPx: Int,
    density: androidx.compose.ui.unit.Density
): Map<NavButtonType, Offset> {
    val metrics = android.util.DisplayMetrics().apply {
        widthPixels = viewportSize.width
        heightPixels = viewportSize.height - navBarBottomPx
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

private const val TAG = "PositionOverlay"
