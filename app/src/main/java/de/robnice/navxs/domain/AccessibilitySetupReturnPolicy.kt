package de.robnice.navxs.domain

/**
 * Decides whether the app should be brought back to the foreground once the
 * accessibility service gets enabled. Only applies when the user explicitly
 * agreed in the disclosure shortly before, so e.g. a reboot never opens the app.
 */
class AccessibilitySetupReturnPolicy(
    private val clock: () -> Long = System::currentTimeMillis
) {
    @Volatile
    private var consentTimestamp: Long? = null

    fun markSettingsOpenedAfterConsent() {
        consentTimestamp = clock()
    }

    fun consumeShouldReturnToApp(): Boolean {
        val timestamp = consentTimestamp ?: return false
        consentTimestamp = null
        return clock() - timestamp <= TIMEOUT_MS
    }

    companion object {
        const val TIMEOUT_MS = 10 * 60 * 1000L

        val shared = AccessibilitySetupReturnPolicy()
    }
}
