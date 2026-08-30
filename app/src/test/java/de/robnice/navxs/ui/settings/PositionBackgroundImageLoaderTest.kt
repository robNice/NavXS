package de.robnice.navxs.ui.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PositionBackgroundImageLoaderTest {
    @Test
    fun decodeSizeDownscalesLargeImageWithoutChangingAspectRatio() {
        val size = calculatePositionBackgroundDecodeSize(
            sourceWidth = 2160,
            sourceHeight = 4800,
            maximumWidth = 1080,
            maximumHeight = 2400
        )

        assertThat(size).isEqualTo(1080 to 2400)
    }

    @Test
    fun decodeSizeDoesNotUpscaleSmallImage() {
        val size = calculatePositionBackgroundDecodeSize(
            sourceWidth = 540,
            sourceHeight = 1200,
            maximumWidth = 1080,
            maximumHeight = 2400
        )

        assertThat(size).isEqualTo(540 to 1200)
    }

    @Test
    fun decodeSizeAlwaysReturnsPositiveDimensions() {
        val size = calculatePositionBackgroundDecodeSize(
            sourceWidth = 0,
            sourceHeight = 0,
            maximumWidth = 0,
            maximumHeight = 0
        )

        assertThat(size).isEqualTo(1 to 1)
    }
}
