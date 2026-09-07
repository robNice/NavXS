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
    fun idleOpacityNeverExceedsConfiguredOpacity() {
        listOf(0f, 0.05f, 0.5f, 1f).forEach { configured ->
            assertThat(idleOpacity(configured)).isAtMost(configured)
        }
    }

    @Test
    fun idleOpacityReducesVisibleIcons() {
        assertThat(idleOpacity(1f)).isEqualTo(IDLE_OPACITY_FACTOR)
        assertThat(idleOpacity(0.8f)).isWithin(0.0001f).of(0.8f * IDLE_OPACITY_FACTOR)
    }

    @Test
    fun idleDelayStaysInSpecifiedRange() {
        assertThat(IDLE_DELAY_MS).isAtLeast(30_000L)
        assertThat(IDLE_DELAY_MS).isAtMost(60_000L)
    }

    @Test
    fun touchTargetGrowsForLargeIcons() {
        val density = 1f

        assertThat(iconSizePx(sizePercent = 300, density = density)).isEqualTo(96)
        assertThat(touchTargetPx(sizePercent = 300, density = density)).isEqualTo(96)
    }
}
