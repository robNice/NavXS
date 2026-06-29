package de.robnice.navxs.overlay

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OverlayControllerTest {
    @Test
    fun touchTargetUsesMinimumSquareForDefaultSize() {
        val density = 1f

        assertThat(iconSizePx(sizePercent = 100, density = density)).isEqualTo(32)
        assertThat(touchTargetPx(sizePercent = 100, density = density)).isEqualTo(56)
    }

    @Test
    fun touchTargetGrowsForLargeIcons() {
        val density = 1f

        assertThat(iconSizePx(sizePercent = 300, density = density)).isEqualTo(96)
        assertThat(touchTargetPx(sizePercent = 300, density = density)).isEqualTo(96)
    }

    @Test
    fun overlayPositionIsClampedIntoViewport() {
        assertThat(clampOverlayPositionPx(positionPx = -10, viewportPx = 320, touchTargetPx = 56)).isEqualTo(0)
        assertThat(clampOverlayPositionPx(positionPx = 300, viewportPx = 320, touchTargetPx = 56)).isEqualTo(264)
    }

    @Test
    fun overlayPositionFallsBackToZeroWhenButtonIsLargerThanViewport() {
        assertThat(clampOverlayPositionPx(positionPx = 40, viewportPx = 48, touchTargetPx = 96)).isEqualTo(0)
    }
}
