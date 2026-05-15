package de.robnice.navxs.domain

import com.google.common.truth.Truth.assertThat
import de.robnice.navxs.data.models.AppSelection
import org.junit.Test

class OverlayVisibilityPolicyTest {
    private val policy = OverlayVisibilityPolicy("de.robnice.navxs")

    @Test
    fun selectedForegroundAppShowsOverlay() {
        val result = policy.shouldShowOverlay(
            accessibilityEnabled = true,
            foregroundPackageName = "com.example.target",
            selectedApps = setOf("com.example.target"),
            appUiInForeground = false
        )

        assertThat(result).isTrue()
    }

    @Test
    fun ownAppSuppressesOverlay() {
        val result = policy.shouldShowOverlay(
            accessibilityEnabled = true,
            foregroundPackageName = "de.robnice.navxs",
            selectedApps = setOf("de.robnice.navxs"),
            appUiInForeground = false
        )

        assertThat(result).isFalse()
    }

    @Test
    fun foregroundUiSuppressesOverlayEvenForSelectedApp() {
        val result = policy.shouldShowOverlay(
            accessibilityEnabled = true,
            foregroundPackageName = "com.example.target",
            selectedApps = setOf("com.example.target"),
            appUiInForeground = true
        )

        assertThat(result).isFalse()
    }

    @Test
    fun sortingPlacesEnabledAppsFirstThenAlphabetically() {
        val sorted = policy.sortSelections(
            selections = listOf(
                AppSelection("b.package", false),
                AppSelection("a.package", true),
                AppSelection("c.package", true)
            ),
            labels = mapOf(
                "a.package" to "Alpha",
                "b.package" to "Beta",
                "c.package" to "Charlie"
            )
        )

        assertThat(sorted.map { it.packageName }).containsExactly("a.package", "c.package", "b.package").inOrder()
    }
}
