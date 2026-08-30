package de.robnice.navxs.ui.settings

import android.content.ContentResolver
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

internal class PositionBackgroundImageLoader(
    private val contentResolver: ContentResolver
) {
    suspend fun load(
        uri: Uri,
        maximumWidth: Int,
        maximumHeight: Int
    ): ImageBitmap = decodeBitmap(uri, maximumWidth, maximumHeight).asImageBitmap()

    suspend fun validate(uri: Uri) {
        decodeBitmap(uri, VALIDATION_SIZE_PX, VALIDATION_SIZE_PX).recycle()
    }

    private suspend fun decodeBitmap(
        uri: Uri,
        maximumWidth: Int,
        maximumHeight: Int
    ) = withContext(Dispatchers.IO) {
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val (targetWidth, targetHeight) = calculatePositionBackgroundDecodeSize(
                sourceWidth = info.size.width,
                sourceHeight = info.size.height,
                maximumWidth = maximumWidth,
                maximumHeight = maximumHeight
            )
            decoder.setTargetSize(targetWidth, targetHeight)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }
}

internal fun calculatePositionBackgroundDecodeSize(
    sourceWidth: Int,
    sourceHeight: Int,
    maximumWidth: Int,
    maximumHeight: Int
): Pair<Int, Int> {
    if (sourceWidth <= 0 || sourceHeight <= 0 || maximumWidth <= 0 || maximumHeight <= 0) {
        return 1 to 1
    }
    val scale = minOf(
        1.0,
        maximumWidth.toDouble() / sourceWidth,
        maximumHeight.toDouble() / sourceHeight
    )
    return (sourceWidth * scale).roundToInt().coerceAtLeast(1) to
        (sourceHeight * scale).roundToInt().coerceAtLeast(1)
}

private const val VALIDATION_SIZE_PX = 64
