package de.robnice.navxs.ui.settings

import android.graphics.Color.HSVToColor
import android.graphics.Color.colorToHSV
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.robnice.navxs.R
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ColorPicker(
    selectedColor: Long,
    enabled: Boolean,
    onSelected: (Long) -> Unit
) {
    var dialogOpen by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(Color(selectedColor), CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(enabled = enabled) { dialogOpen = true },
        contentAlignment = Alignment.Center
    ) {}
    if (dialogOpen) {
        ColorPickerDialog(
            initialColor = selectedColor,
            onDismiss = { dialogOpen = false },
            onConfirm = {
                onSelected(it)
                dialogOpen = false
            }
        )
    }
}

@Composable
private fun ColorPickerDialog(
    initialColor: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val initialHsv = remember(initialColor) { initialHsv(initialColor) }
    var hue by remember(initialColor) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(initialColor) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(initialColor) { mutableFloatStateOf(initialHsv[2]) }
    val previewColor = remember(hue, saturation, value) { hsvToColorLong(hue, saturation, value) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_colour)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color(previewColor), CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    text = stringResource(R.string.settings_color_hex, formatColor(previewColor)),
                    style = MaterialTheme.typography.bodyMedium
                )
                ColorWheel(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onChange = { nextHue, nextSaturation ->
                        hue = nextHue
                        saturation = nextSaturation
                    }
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.settings_color_brightness, (value * 100).roundToInt()),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = value,
                        valueRange = 0f..1f,
                        onValueChange = { value = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(previewColor) }) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
private fun ColorWheel(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .size(220.dp)
            .pointerInput(value) {
                detectDragGestures(
                    onDragStart = { offset ->
                        updateWheelSelection(offset, size.width.toFloat(), size.height.toFloat(), onChange)
                    }
                ) { change, _ ->
                    change.consume()
                    updateWheelSelection(change.position, size.width.toFloat(), size.height.toFloat(), onChange)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val saturationSteps = 24
            val hueSteps = 72

            for (satIndex in saturationSteps downTo 1) {
                val sat = satIndex / saturationSteps.toFloat()
                val ringRadius = radius * sat
                val strokeWidth = radius / saturationSteps + 1.dp.toPx()
                for (hueIndex in 0 until hueSteps) {
                    val startAngle = hueIndex * (360f / hueSteps)
                    drawArc(
                        color = Color(hsvToColorLong(startAngle, sat, value)),
                        startAngle = startAngle,
                        sweepAngle = 360f / hueSteps + 1f,
                        useCenter = false,
                        topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                        size = androidx.compose.ui.geometry.Size(ringRadius * 2, ringRadius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                }
            }

            val selectorRadius = radius * saturation
            val selectorAngle = Math.toRadians(hue.toDouble())
            val selectorCenter = Offset(
                x = center.x + (cos(selectorAngle) * selectorRadius).toFloat(),
                y = center.y + (sin(selectorAngle) * selectorRadius).toFloat()
            )
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = selectorCenter,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = 12.dp.toPx(),
                center = selectorCenter,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

private fun updateWheelSelection(
    offset: Offset,
    width: Float,
    height: Float,
    onChange: (Float, Float) -> Unit
) {
    if (width <= 0f || height <= 0f) return
    val centerX = width / 2f
    val centerY = height / 2f
    val dx = offset.x - centerX
    val dy = offset.y - centerY
    val radius = minOf(width, height) / 2f
    val distance = sqrt(dx * dx + dy * dy).coerceAtMost(radius)
    val saturation = (distance / radius).coerceIn(0f, 1f)
    val angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
    val hue = (angle + 360f).mod(360f)
    onChange(hue, saturation)
}

private fun initialHsv(color: Long): FloatArray {
    val hsv = FloatArray(3)
    colorToHSV(color.toInt(), hsv)
    return hsv
}

private fun hsvToColorLong(hue: Float, saturation: Float, value: Float): Long =
    (HSVToColor(floatArrayOf(hue, saturation, value)).toLong() and 0xFFFFFFFFL)

private fun formatColor(color: Long): String = String.format("#%06X", color and 0xFFFFFF)
