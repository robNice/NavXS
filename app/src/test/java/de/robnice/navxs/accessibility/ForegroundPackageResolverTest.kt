package de.robnice.navxs.accessibility

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ForegroundPackageResolverTest {
    private val resolver = ForegroundPackageResolver(
        ownPackageName = "de.robnice.navxs",
        ignoredPackages = setOf("com.android.systemui")
    )

    @Test
    fun recentLauncherEventTemporarilyOverridesStaleRootPackage() {
        val start = 1_000L

        resolver.onAccessibilityPackage(
            packageName = "de.robnice.homeshoplist",
            appUiInForeground = false,
            nowMs = start
        )

        val fromLauncherEvent = resolver.onAccessibilityPackage(
            packageName = "com.google.android.apps.nexuslauncher",
            appUiInForeground = false,
            nowMs = start + 50L
        )

        val staleRootResult = resolver.resolveFromRoot(
            rootPackage = "de.robnice.homeshoplist",
            appUiInForeground = false,
            nowMs = start + 150L
        )

        assertThat(fromLauncherEvent).isEqualTo("com.google.android.apps.nexuslauncher")
        assertThat(staleRootResult).isEqualTo("com.google.android.apps.nexuslauncher")
    }

    @Test
    fun rootPackageCanWinAgainAfterBlockWindowExpires() {
        val start = 1_000L

        resolver.onAccessibilityPackage(
            packageName = "com.google.android.apps.nexuslauncher",
            appUiInForeground = false,
            nowMs = start
        )

        val resolved = resolver.resolveFromRoot(
            rootPackage = "de.robnice.homeshoplist",
            appUiInForeground = false,
            nowMs = start + 1_500L
        )

        assertThat(resolved).isEqualTo("de.robnice.homeshoplist")
    }
}
