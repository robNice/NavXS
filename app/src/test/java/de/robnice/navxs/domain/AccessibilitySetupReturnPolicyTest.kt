package de.robnice.navxs.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilitySetupReturnPolicyTest {

    @Test
    fun noReturnWithoutPriorConsent() {
        val policy = AccessibilitySetupReturnPolicy(clock = { 1_000L })
        assertFalse(policy.consumeShouldReturnToApp())
    }

    @Test
    fun returnsOnceAfterConsentWithinTimeout() {
        var now = 0L
        val policy = AccessibilitySetupReturnPolicy(clock = { now })
        policy.markSettingsOpenedAfterConsent()
        now = 60_000L
        assertTrue(policy.consumeShouldReturnToApp())
        assertFalse(policy.consumeShouldReturnToApp())
    }

    @Test
    fun noReturnAfterTimeoutExpired() {
        var now = 0L
        val policy = AccessibilitySetupReturnPolicy(clock = { now })
        policy.markSettingsOpenedAfterConsent()
        now = AccessibilitySetupReturnPolicy.TIMEOUT_MS + 1
        assertFalse(policy.consumeShouldReturnToApp())
    }
}
