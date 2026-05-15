package de.robnice.navxs.domain

import de.robnice.navxs.data.models.AppSelection

class OverlayVisibilityPolicy(private val ownPackageName: String) {
    fun shouldShowOverlay(
        accessibilityEnabled: Boolean,
        foregroundPackageName: String?,
        selectedApps: Set<String>,
        appUiInForeground: Boolean
    ): Boolean {
        if (!accessibilityEnabled) return false
        if (selectedApps.isEmpty()) return false
        if (appUiInForeground) return false
        if (foregroundPackageName.isNullOrBlank()) return false
        if (foregroundPackageName == ownPackageName) return false
        return selectedApps.contains(foregroundPackageName)
    }

    fun sortSelections(selections: List<AppSelection>, labels: Map<String, String>): List<AppSelection> {
        return selections.sortedWith(
            compareByDescending<AppSelection> { it.enabled }
                .thenBy { labels[it.packageName]?.lowercase().orEmpty() }
        )
    }
}
