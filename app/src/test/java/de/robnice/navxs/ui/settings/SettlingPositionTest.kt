package de.robnice.navxs.ui.settings

import androidx.compose.ui.geometry.Offset
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettlingPositionTest {
    @Test
    fun settlingIsResolvedWhenThePersistedPixelPositionMatches() {
        // A drag ends on fractional pixels, the repository stores whole pixels. Both describe the
        // same spot, so the settling state has to be released - otherwise the preview keeps drawing
        // the button at the frozen settling position and ignores later precision steps.
        val settling = Offset(123.4f, 200.7f)
        val persisted = Offset(123f, 201f)

        assertThat(matchesPersistedPosition(persisted = persisted, settling = settling)).isTrue()
    }

    @Test
    fun settlingStaysWhileTheRepositoryStillReportsTheOldPosition() {
        val settling = Offset(500f, 900f)
        val persisted = Offset(120f, 300f)

        assertThat(matchesPersistedPosition(persisted = persisted, settling = settling)).isFalse()
    }

    @Test
    fun missingPositionsNeverCount() {
        assertThat(matchesPersistedPosition(persisted = null, settling = Offset(1f, 2f))).isFalse()
        assertThat(matchesPersistedPosition(persisted = Offset(1f, 2f), settling = null)).isFalse()
    }

    @Test
    fun committedPositionsAreSnappedToWholePixels() {
        assertThat(roundToPixel(Offset(123.4f, 200.7f))).isEqualTo(Offset(123f, 201f))
    }
}
