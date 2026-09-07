package de.robnice.navxs.ui.settings

import android.app.Activity
import android.content.ContextWrapper
import android.net.Uri
import android.util.Log
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import de.robnice.navxs.R
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.SettingsRepository
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlaySettings
import de.robnice.navxs.overlay.OverlayViewport
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt

@Composable
fun SettingsPositionOverlay(
    settings: OverlaySettings,
    targetLabel: String,
    precisionOpen: Boolean,
    positionBackgroundUri: String?,
    positionBackgroundAlpha: Int,
    positionBackgroundDialogOpen: Boolean,
    onSelectButton: (NavButtonType) -> Unit,
    onCommitMoveButtonPosition: (NavButtonType, Int, Int) -> Unit,
    onCloseEditMode: () -> Unit,
    onOpenPrecision: () -> Unit,
    onClosePrecision: () -> Unit,
    onStepChange: (Int) -> Unit,
    onPrecisionMove: (NavButtonType, Int, Int) -> Unit,
    onResetPosition: (Map<NavButtonType, Pair<Int, Int>>) -> Unit,
    onRequestPositionBackground: () -> Unit,
    onClearPositionBackground: () -> Unit,
    onPositionBackgroundAlphaChange: (Int) -> Unit,
    onPositionBackgroundDialogOpenChange: (Boolean) -> Unit,
    onPositionBackgroundLoadError: (String) -> Unit
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val displayMetrics = remember(context) { OverlayViewport.metrics(context) }
    val viewportWidthPx = displayMetrics.widthPixels
    val viewportHeightPx = displayMetrics.heightPixels
    val rawNavBarBottomPx = WindowInsets.navigationBars.getBottom(density)
    val stableNavBarBottomPx = remember { rawNavBarBottomPx }
    val backgroundImageLoader = remember(context) {
        PositionBackgroundImageLoader(context.applicationContext.contentResolver)
    }
    val backgroundLoadState by produceState<PositionBackgroundLoadState>(
        initialValue = PositionBackgroundLoadState.Empty,
        key1 = positionBackgroundUri,
        key2 = viewportWidthPx,
        key3 = viewportHeightPx
    ) {
        value = if (positionBackgroundUri == null) {
            PositionBackgroundLoadState.Empty
        } else {
            try {
                PositionBackgroundLoadState.Loaded(
                    backgroundImageLoader.load(
                        uri = Uri.parse(positionBackgroundUri),
                        maximumWidth = viewportWidthPx,
                        maximumHeight = viewportHeightPx
                    )
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.w(TAG, "Could not load positioning background", exception)
                PositionBackgroundLoadState.Failed(positionBackgroundUri)
            }
        }
    }
    LaunchedEffect(backgroundLoadState) {
        (backgroundLoadState as? PositionBackgroundLoadState.Failed)?.let { failed ->
            onPositionBackgroundLoadError(failed.uri)
        }
    }
    val positionBackgroundImage =
        (backgroundLoadState as? PositionBackgroundLoadState.Loaded)?.image
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
    var positionBackgroundAlphaPercent by remember(positionBackgroundAlpha) {
        mutableStateOf(positionBackgroundAlpha.toFloat())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LaunchedEffect(settings.buttons, viewportWidthPx, viewportHeightPx, isDragging, dragButtonType, precisionOpen) {
            val persistedPositions = settings.buttons.mapValues { (_, button) ->
                Offset(button.positionXPx.toFloat(), button.positionYPx.toFloat())
            }
            if (
                matchesPersistedPosition(
                    persisted = settlingButtonType?.let(persistedPositions::get),
                    settling = settlingPosition
                )
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
                positionBackgroundImage = positionBackgroundImage,
                positionBackgroundAlpha = positionBackgroundAlphaPercent / 100f,
                buttonPositions = localButtonPositions,
                draggedButtonType = dragButtonType ?: settlingButtonType,
                draggedButtonPosition = dragPreviewPosition ?: settlingPosition,
                selectedButtonTypeOverride = dragButtonType ?: localSelectedButtonType,
                onSelectButton = {
                    localSelectedButtonType = it
                    onSelectButton(it)
                },
                onDragStarted = { type, startPosition ->
                    dragButtonType = type
                    localSelectedButtonType = type
                    isDragging = true
                    dragPreviewPosition = startPosition
                    onSelectButton(type)
                },
                onMoveSelectedButton = { deltaX, deltaY ->
                    val type = dragButtonType ?: return@ButtonPreviewArea
                    val currentPosition = dragPreviewPosition ?: localButtonPositions[type] ?: return@ButtonPreviewArea
                    val nextPosition = Offset(
                        x = currentPosition.x + deltaX,
                        y = currentPosition.y + deltaY,
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
                        "commitDrag type=$commitType target=$localPosition"
                    )
                    val committedPosition = roundToPixel(localPosition)
                    localButtonPositions = localButtonPositions + (commitType to committedPosition)
                    settlingButtonType = commitType
                    settlingPosition = committedPosition
                    dragPreviewPosition = null
                    if (committedPosition.x.roundToInt() != persistedButton.positionXPx || committedPosition.y.roundToInt() != persistedButton.positionYPx) {
                        onCommitMoveButtonPosition(
                            commitType,
                            committedPosition.x.roundToInt(),
                            committedPosition.y.roundToInt()
                        )
                    }
                    isDragging = false
                }
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                    )
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    tonalElevation = 3.dp,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 420.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = targetLabel,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("position_target_label")
                        )
                        Text(
                            text = stringResource(R.string.settings_drag_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("position_drag_hint")
                        )
                    }
                }
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f)
                    )
                ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    FilledTonalIconButton(onClick = onCloseEditMode) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.settings_done)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = {
                            val resetPositions = navBarResetPositions(
                                settings = settings,
                                viewportSize = IntSize(viewportWidthPx, viewportHeightPx),
                                navBarBottomPx = stableNavBarBottomPx,
                                density = density
                            )
                            settlingButtonType = null
                            settlingPosition = null
                            localButtonPositions = localButtonPositions + resetPositions
                            onResetPosition(
                                resetPositions.mapValues { (_, position) ->
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
                    FilledTonalIconButton(
                        onClick = onOpenPrecision,
                        enabled = !precisionOpen,
                        modifier = Modifier.testTag("precision_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.OpenWith,
                            contentDescription = stringResource(R.string.settings_open_precision)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = {
                            onClosePrecision()
                            onPositionBackgroundDialogOpenChange(true)
                        },
                        modifier = Modifier.testTag("position_background_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = stringResource(R.string.settings_position_background_manage)
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
                            val newPos = Offset(currentPos.x + dx, currentPos.y + dy)
                            localButtonPositions = localButtonPositions + (type to newPos)
                            settlingButtonType = null
                            settlingPosition = null
                            onPrecisionMove(type, newPos.x.roundToInt(), newPos.y.roundToInt())
                        },
                        onResetPosition = {
                            val resetPositions = navBarResetPositions(
                                settings = settings,
                                viewportSize = IntSize(viewportWidthPx, viewportHeightPx),
                                navBarBottomPx = stableNavBarBottomPx,
                                density = density
                            )
                            settlingButtonType = null
                            settlingPosition = null
                            localButtonPositions = localButtonPositions + resetPositions
                            onResetPosition(
                                resetPositions.mapValues { (_, position) ->
                                    position.x.roundToInt() to position.y.roundToInt()
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    if (positionBackgroundDialogOpen) {
        PositionBackgroundDialog(
            alphaPercent = positionBackgroundAlphaPercent,
            onAlphaPercentChange = { positionBackgroundAlphaPercent = it },
            onAlphaPercentCommit = { onPositionBackgroundAlphaChange(it.roundToInt()) },
            hasBackground = positionBackgroundUri != null,
            onDismiss = { onPositionBackgroundDialogOpenChange(false) },
            onReset = {
                onPositionBackgroundDialogOpenChange(false)
                onClearPositionBackground()
            },
            onSelect = onRequestPositionBackground
        )
    }
}

@Composable
internal fun PositionBackgroundDialog(
    hasBackground: Boolean,
    alphaPercent: Float,
    onAlphaPercentChange: (Float) -> Unit,
    onAlphaPercentCommit: (Float) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onSelect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("position_background_dialog"),
        title = { Text(stringResource(R.string.settings_position_background_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("position_background_reset")
                ) {
                    Text(stringResource(R.string.settings_position_background_reset))
                }
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("position_background_select")
                ) {
                    Text(stringResource(R.string.settings_position_background_select))
                }
                if (hasBackground) {
                    Text(
                        text = stringResource(
                            R.string.settings_position_background_opacity,
                            alphaPercent.roundToInt()
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Slider(
                        value = alphaPercent,
                        onValueChange = onAlphaPercentChange,
                        onValueChangeFinished = { onAlphaPercentCommit(alphaPercent) },
                        valueRange = SettingsRepository.MinPositionBackgroundAlpha.toFloat()..
                            SettingsRepository.MaxPositionBackgroundAlpha.toFloat(),
                        modifier = Modifier.testTag("position_background_opacity_slider")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_ok))
            }
        }
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

private sealed interface PositionBackgroundLoadState {
    data object Empty : PositionBackgroundLoadState
    data class Loaded(val image: ImageBitmap) : PositionBackgroundLoadState
    data class Failed(val uri: String) : PositionBackgroundLoadState
}


/**
 * A drag ends on fractional pixels while the repository stores whole pixels. Comparing the raw
 * values would never match, the settling state would never be released, and the preview would keep
 * drawing the button at the frozen settling position - which is why precision steps looked as if
 * they did nothing until the editor was reopened.
 */
internal fun matchesPersistedPosition(persisted: Offset?, settling: Offset?): Boolean {
    if (persisted == null || settling == null) return false
    return roundToPixel(persisted) == roundToPixel(settling)
}

/** Snaps a position to whole pixels, the resolution the repository actually stores. */
internal fun roundToPixel(position: Offset): Offset =
    Offset(position.x.roundToInt().toFloat(), position.y.roundToInt().toFloat())
