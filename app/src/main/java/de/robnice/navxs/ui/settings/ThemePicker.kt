package de.robnice.navxs.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HouseSiding
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.West
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import de.robnice.navxs.data.models.ButtonTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

@Composable
fun ThemePicker(
    themes: List<ButtonTheme>,
    selectedThemeId: String,
    enabled: Boolean,
    onThemeSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(themes, key = { it.id }) { theme ->
            Card(
                modifier = Modifier
                    .alpha(if (enabled) 1f else 0.45f)
                    .clickable(enabled = enabled) { onThemeSelected(theme.id) },
                border = BorderStroke(
                    width = if (theme.id == selectedThemeId) 2.dp else 1.dp,
                    color = if (theme.id == selectedThemeId) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
            ) {
                Box(
                    modifier = Modifier.size(58.dp),
                    contentAlignment = Alignment.Center
                ) {
                    resolveThemeIcon(theme)?.let { icon ->
                        Icon(imageVector = icon, contentDescription = theme.id)
                    } ?: androidx.compose.material3.Text(theme.fallbackText)
                }
            }
        }
    }
}

fun resolveThemeIcon(theme: ButtonTheme): ImageVector? = when (theme.vectorAssetName) {
    "ArrowBackNew" -> arrowBackNew
    "ArrowBackNewFilled" -> arrowBackNewFilled
    "ArrowLeft" -> arrowLeft
    "ArrowBackIosNew" -> arrowBackIosNew
    "ArrowBack" -> arrowBackClassic
    "West" -> Icons.Filled.West
    "ChevronLeft" -> Icons.Rounded.ChevronLeft
    "KeyboardArrowLeft" -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
    "PlayArrowLeft" -> Icons.Rounded.PlayArrow
    "Home" -> Icons.Filled.Home
    "HomeOutlined" -> Icons.Outlined.Home
    "HouseSiding" -> Icons.Filled.HouseSiding
    "Circle" -> Icons.Filled.Circle
    "RadioButtonUnchecked" -> Icons.Filled.RadioButtonUnchecked
    "RoundedSquare" -> Icons.Filled.RoundedCorner
    "RoundedSquareOutline" -> Icons.Rounded.CheckBoxOutlineBlank
    "RoundedSquareFilled" -> Icons.Rounded.Stop
    "HomeSquircleOutline" -> homeSquircleOutline
    "HomeSquircleFilled" -> homeSquircleFilled
    "CropSquare" -> Icons.Filled.CropSquare
    "RecentsLinesHorizontal" -> recentsLinesHorizontal
    "RecentsLinesVertical" -> recentsLinesVertical
    "Layers" -> Icons.Filled.Layers
    "DashboardCustomize" -> Icons.Filled.DashboardCustomize
    "Apps" -> Icons.Filled.Widgets
    "ViewAgenda" -> Icons.Filled.ViewAgenda
    else -> null
}

private val arrowBackNew: ImageVector by lazy {
    materialSymbol(name = "ArrowBackNew") {
        moveTo(16.8872f, 20.1925f)
        lineTo(3.1372f, 11.4425f)
        lineTo(16.8872f, 2.6925f)
        verticalLineTo(20.1925f)
        close()
        moveTo(14.3872f, 15.63f)
        verticalLineTo(7.255f)
        lineTo(7.8247f, 11.4425f)
        lineTo(14.3872f, 15.63f)
        close()
    }
}

private val arrowBackNewFilled: ImageVector by lazy {
    materialSymbol(name = "ArrowBackNewFilled") {
        moveTo(16.8872f, 20.1925f)
        lineTo(3.1372f, 11.4425f)
        lineTo(16.8872f, 2.6925f)
        verticalLineTo(20.1925f)
        close()
    }
}

private val arrowLeft: ImageVector by lazy {
    materialSymbol(name = "ArrowLeft") {
        moveTo(14.3872f, 17.6925f)
        lineTo(8.1372f, 11.4425f)
        lineTo(14.3872f, 5.1925f)
        verticalLineTo(17.6925f)
        close()
    }
}

private val arrowBackIosNew: ImageVector by lazy {
    materialSymbol(name = "ArrowBackIosNew") {
        moveTo(16.8872f, 23.9425f)
        lineTo(4.3872f, 11.4425f)
        lineTo(16.8872f, -1.0575f)
        lineTo(19.1059f, 1.1612f)
        lineTo(8.8247f, 11.4425f)
        lineTo(19.1059f, 21.7237f)
        lineTo(16.8872f, 23.9425f)
        close()
    }
}

private val arrowBackClassic: ImageVector by lazy {
    materialSymbol(name = "ArrowBack") {
        moveTo(6.6684f, 12.6925f)
        lineTo(13.6684f, 19.6925f)
        lineTo(11.8872f, 21.4425f)
        lineTo(1.8872f, 11.4425f)
        lineTo(11.8872f, 1.4425f)
        lineTo(13.6684f, 3.1925f)
        lineTo(6.6684f, 10.1925f)
        horizontalLineTo(21.8872f)
        verticalLineTo(12.6925f)
        horizontalLineTo(6.6684f)
        close()
    }
}

private fun materialSymbol(
    name: String,
    pathBuilder: PathBuilder.() -> Unit
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
    autoMirror = true
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero,
        pathBuilder = pathBuilder
    )
}.build()

private val homeSquircleOutline: ImageVector by lazy {
    ImageVector.Builder(
        name = "HomeSquircleOutline",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            pathFillType = PathFillType.NonZero
        ) {
            addSquirclePath(centerX = 12f, centerY = 12f, radius = 7.35, exponent = 3.6)
        }
    }.build()
}

private val homeSquircleFilled: ImageVector by lazy {
    ImageVector.Builder(
        name = "HomeSquircleFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero
        ) {
            addSquirclePath(centerX = 12f, centerY = 12f, radius = 7.35, exponent = 3.6)
        }
    }.build()
}

private val recentsLinesHorizontal: ImageVector by lazy {
    ImageVector.Builder(
        name = "RecentsLinesHorizontal",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            addRecentsLinePath(left = 5f, top = 6f, right = 19f, bottom = 8f)
            addRecentsLinePath(left = 5f, top = 11f, right = 19f, bottom = 13f)
            addRecentsLinePath(left = 5f, top = 16f, right = 19f, bottom = 18f)
        }
    }.build()
}

private val recentsLinesVertical: ImageVector by lazy {
    ImageVector.Builder(
        name = "RecentsLinesVertical",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black), pathFillType = PathFillType.NonZero) {
            addRecentsLinePath(left = 6f, top = 5f, right = 8f, bottom = 19f)
            addRecentsLinePath(left = 11f, top = 5f, right = 13f, bottom = 19f)
            addRecentsLinePath(left = 16f, top = 5f, right = 18f, bottom = 19f)
        }
    }.build()
}

private fun PathBuilder.addSquirclePath(
    centerX: Float,
    centerY: Float,
    radius: Double,
    exponent: Double,
    steps: Int = 96
) {
    val power = 2.0 / exponent
    for (step in 0..steps) {
        val angle = (step.toDouble() / steps.toDouble()) * (PI * 2.0)
        val cosValue = cos(angle)
        val sinValue = sin(angle)
        val x = centerX + (radius * sign(cosValue) * abs(cosValue).pow(power)).toFloat()
        val y = centerY + (radius * sign(sinValue) * abs(sinValue).pow(power)).toFloat()
        if (step == 0) {
            moveTo(x, y)
        } else {
            lineTo(x, y)
        }
    }
    close()
}

private fun PathBuilder.addRecentsLinePath(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float
) {
    moveTo(left, top)
    lineTo(right, top)
    lineTo(right, bottom)
    lineTo(left, bottom)
    close()
}
