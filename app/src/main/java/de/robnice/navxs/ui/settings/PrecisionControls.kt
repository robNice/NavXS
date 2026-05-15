package de.robnice.navxs.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.robnice.navxs.R
import de.robnice.navxs.data.models.OverlaySettings
import kotlinx.coroutines.delay

@Composable
fun PrecisionControls(
    settings: OverlaySettings,
    onDismiss: () -> Unit,
    onStepChange: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onResetPosition: () -> Unit
) {
    val selectedButton = settings.buttons.getValue(settings.selectedButtonType)
    var panelOffset by remember { mutableStateOf(IntOffset.Zero) }
    Card(
        modifier = Modifier
            .fillMaxWidth(0.76f)
            .offset { panelOffset }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    panelOffset = IntOffset(
                        x = panelOffset.x + dragAmount.x.toInt(),
                        y = panelOffset.y + dragAmount.y.toInt()
                    )
                }
            },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Spacer(
                    modifier = Modifier
                        .size(width = 30.dp, height = 4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(99.dp))
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_precision),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.settings_done),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RepeatMoveButton(
                        icon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null) },
                        onRepeat = { onMove(0, -settings.precisionStepPx) }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RepeatMoveButton(
                            icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) },
                            onRepeat = { onMove(-settings.precisionStepPx, 0) }
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${stringResource(R.string.settings_position_x)}: ${selectedButton.positionXPx} px",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${stringResource(R.string.settings_position_y)}: ${selectedButton.positionYPx} px",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RepeatMoveButton(
                            icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                            onRepeat = { onMove(settings.precisionStepPx, 0) }
                        )
                    }
                    RepeatMoveButton(
                        icon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null) },
                        onRepeat = { onMove(0, settings.precisionStepPx) }
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.settings_precision_step),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(1, 5, 10).forEach { step ->
                        OutlinedButton(
                            onClick = { onStepChange(step) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (settings.precisionStepPx == step) {
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.background
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (settings.precisionStepPx == step) {
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.position_value, step),
                                fontSize = 13.sp,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = onResetPosition,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = stringResource(R.string.settings_reset_position),
                    modifier = Modifier.padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun RepeatMoveButton(
    icon: @Composable () -> Unit,
    onRepeat: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val buttonShape = RoundedCornerShape(16.dp)
    LaunchedEffect(pressed) {
        if (pressed) {
            onRepeat()
            delay(250)
            while (pressed) {
                onRepeat()
                delay(70)
            }
        }
    }
    FilledTonalIconButton(
        onClick = onRepeat,
        interactionSource = interactionSource,
        shape = buttonShape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        ),
        modifier = Modifier
            .size(46.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, buttonShape)
    ) {
        icon()
    }
}
