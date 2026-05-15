package de.robnice.navxs.accessibility

class ForegroundPackageResolver(
    private val ownPackageName: String,
    private val ignoredPackages: Set<String>,
    private val rootOverrideBlockMs: Long = 1_200L
) {
    private var eventPackage: String? = null
    private var eventTimestampMs: Long = 0L
    private var fallbackPackage: String? = null

    fun onAccessibilityPackage(
        packageName: String?,
        appUiInForeground: Boolean,
        nowMs: Long
    ): String? {
        if (isUsable(packageName, appUiInForeground)) {
            eventPackage = packageName
            eventTimestampMs = nowMs
            fallbackPackage = packageName
        }
        return currentPackage(appUiInForeground, nowMs)
    }

    fun resolveFromRoot(
        rootPackage: String?,
        appUiInForeground: Boolean,
        nowMs: Long
    ): String? {
        if (isUsable(rootPackage, appUiInForeground)) {
            val recentEventPackage = eventPackage
            val eventStillAuthoritative = recentEventPackage != null &&
                nowMs - eventTimestampMs <= rootOverrideBlockMs &&
                recentEventPackage != rootPackage

            if (!eventStillAuthoritative) {
                fallbackPackage = rootPackage
            }
        }
        return currentPackage(appUiInForeground, nowMs)
    }

    private fun currentPackage(appUiInForeground: Boolean, nowMs: Long): String? {
        val recentEventPackage = eventPackage
        if (recentEventPackage != null && nowMs - eventTimestampMs <= rootOverrideBlockMs) {
            return recentEventPackage
        }
        if (!isUsable(fallbackPackage, appUiInForeground)) {
            return null
        }
        return fallbackPackage
    }

    private fun isUsable(packageName: String?, appUiInForeground: Boolean): Boolean {
        if (packageName.isNullOrBlank()) return false
        if (packageName in ignoredPackages) return false
        if (packageName == ownPackageName && !appUiInForeground) return false
        return true
    }
}
