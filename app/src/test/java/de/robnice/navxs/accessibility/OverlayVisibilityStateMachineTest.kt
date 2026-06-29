package de.robnice.navxs.accessibility

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OverlayVisibilityStateMachineTest {
    private val machine = OverlayVisibilityStateMachine(
        enterDelayMs = 100L,
        leaveDelayMs = 175L,
        verificationIntervalMs = 100L,
        enterVerificationMaxMs = 800L,
        leaveVerificationMaxMs = 1_200L,
        eventFreshMs = 300L
    )

    @Test
    fun selectedAppBecomesVisibleAfterEnterDelayAndConfirmation() {
        val start = 1_000L

        val entering = machine.evaluate(
            snapshot(
                nowMs = start,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start
            )
        )
        val visible = machine.evaluate(
            snapshot(
                nowMs = start + 100L,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start + 100L
            )
        )

        assertThat(entering.state).isEqualTo(OverlayVisibilityStateMachine.State.ENTERING_SELECTED)
        assertThat(entering.overlayVisible).isFalse()
        assertThat(visible.state).isEqualTo(OverlayVisibilityStateMachine.State.VISIBLE)
        assertThat(visible.overlayVisible).isTrue()
    }

    @Test
    fun leavingStateKeepsOverlayVisibleUntilDelayExpires() {
        val start = 1_000L

        machine.evaluate(
            snapshot(
                nowMs = start,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start
            )
        )
        machine.evaluate(
            snapshot(
                nowMs = start + 100L,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start + 100L
            )
        )

        val leaving = machine.evaluate(
            snapshot(
                nowMs = start + 150L,
                resolved = "com.google.android.apps.nexuslauncher",
                root = "com.google.android.apps.nexuslauncher",
                event = "com.google.android.apps.nexuslauncher",
                eventTimestampMs = start + 150L
            )
        )
        val hidden = machine.evaluate(
            snapshot(
                nowMs = start + 325L,
                resolved = "com.google.android.apps.nexuslauncher",
                root = "com.google.android.apps.nexuslauncher",
                event = "com.google.android.apps.nexuslauncher",
                eventTimestampMs = start + 325L
            )
        )

        assertThat(leaving.state).isEqualTo(OverlayVisibilityStateMachine.State.LEAVING_APP)
        assertThat(leaving.overlayVisible).isTrue()
        assertThat(hidden.state).isEqualTo(OverlayVisibilityStateMachine.State.HIDDEN)
        assertThat(hidden.overlayVisible).isFalse()
    }

    @Test
    fun returningSelectedSignalDuringLeavingRestoresVisibleState() {
        val start = 1_000L

        machine.evaluate(
            snapshot(
                nowMs = start,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start
            )
        )
        machine.evaluate(
            snapshot(
                nowMs = start + 100L,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start + 100L
            )
        )
        machine.evaluate(
            snapshot(
                nowMs = start + 150L,
                resolved = "com.google.android.apps.nexuslauncher",
                root = "de.robnice.homeshoplist",
                event = "com.google.android.apps.nexuslauncher",
                eventTimestampMs = start + 150L
            )
        )

        val visibleAgain = machine.evaluate(
            snapshot(
                nowMs = start + 200L,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start + 200L
            )
        )

        assertThat(visibleAgain.state).isEqualTo(OverlayVisibilityStateMachine.State.VISIBLE)
        assertThat(visibleAgain.overlayVisible).isTrue()
    }

    @Test
    fun staleRootAloneDoesNotReenterSelectedAppWhenLauncherSignalsConflict() {
        val start = 1_000L

        val decision = machine.evaluate(
            snapshot(
                nowMs = start,
                resolved = "com.google.android.apps.nexuslauncher",
                root = "de.robnice.homeshoplist",
                event = "com.google.android.apps.nexuslauncher",
                eventTimestampMs = start
            )
        )

        assertThat(decision.state).isEqualTo(OverlayVisibilityStateMachine.State.HIDDEN)
        assertThat(decision.overlayVisible).isFalse()
    }

    @Test
    fun staleRootDoesNotKeepVisibleStateAliveAgainstLauncherSignals() {
        val start = 1_000L

        machine.evaluate(
            snapshot(
                nowMs = start,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start
            )
        )
        machine.evaluate(
            snapshot(
                nowMs = start + 100L,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start + 100L
            )
        )

        val leaving = machine.evaluate(
            snapshot(
                nowMs = start + 150L,
                resolved = "com.google.android.apps.nexuslauncher",
                root = "de.robnice.homeshoplist",
                event = "com.google.android.apps.nexuslauncher",
                eventTimestampMs = start + 150L
            )
        )

        assertThat(leaving.state).isEqualTo(OverlayVisibilityStateMachine.State.LEAVING_APP)
        assertThat(leaving.overlayVisible).isTrue()
    }

    @Test
    fun leavingStateWaitsLongerWhileRootStillShowsTrackedApp() {
        val start = 1_000L

        machine.evaluate(
            snapshot(
                nowMs = start,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start
            )
        )
        machine.evaluate(
            snapshot(
                nowMs = start + 100L,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start + 100L
            )
        )

        machine.evaluate(
            snapshot(
                nowMs = start + 150L,
                resolved = "com.google.android.apps.nexuslauncher",
                root = "de.robnice.homeshoplist",
                event = "com.google.android.apps.nexuslauncher",
                eventTimestampMs = start + 150L
            )
        )

        val stillLeaving = machine.evaluate(
            snapshot(
                nowMs = start + 325L,
                resolved = "com.google.android.apps.nexuslauncher",
                root = "de.robnice.homeshoplist",
                event = "com.google.android.apps.nexuslauncher",
                eventTimestampMs = start + 325L
            )
        )

        assertThat(stillLeaving.state).isEqualTo(OverlayVisibilityStateMachine.State.LEAVING_APP)
        assertThat(stillLeaving.overlayVisible).isTrue()
    }

    @Test
    fun leavingStateStaysActivePastVerificationWindowWhileRootStillMatches() {
        val start = 1_000L

        machine.evaluate(
            snapshot(
                nowMs = start,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start
            )
        )
        machine.evaluate(
            snapshot(
                nowMs = start + 100L,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start + 100L
            )
        )
        machine.evaluate(
            snapshot(
                nowMs = start + 150L,
                resolved = "com.google.android.apps.nexuslauncher",
                root = "de.robnice.homeshoplist",
                event = "com.google.android.apps.nexuslauncher",
                eventTimestampMs = start + 150L
            )
        )

        val stillLeaving = machine.evaluate(
            snapshot(
                nowMs = start + 1_500L,
                resolved = "com.google.android.apps.nexuslauncher",
                root = "de.robnice.homeshoplist",
                event = "com.google.android.apps.nexuslauncher",
                eventTimestampMs = start + 1_500L
            )
        )

        assertThat(stillLeaving.state).isEqualTo(OverlayVisibilityStateMachine.State.LEAVING_APP)
        assertThat(stillLeaving.overlayVisible).isTrue()
    }

    @Test
    fun enteringStateCanBecomeVisibleWithoutRootConfirmationWhenEventAndResolvedAgree() {
        val start = 1_000L

        val entering = machine.evaluate(
            snapshot(
                nowMs = start,
                resolved = "de.robnice.homeshoplist",
                root = null,
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start
            )
        )
        val stillEntering = machine.evaluate(
            snapshot(
                nowMs = start + 100L,
                resolved = "de.robnice.homeshoplist",
                root = null,
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start + 100L
            )
        )
        val visible = machine.evaluate(
            snapshot(
                nowMs = start + 200L,
                resolved = "de.robnice.homeshoplist",
                root = "de.robnice.homeshoplist",
                event = "de.robnice.homeshoplist",
                eventTimestampMs = start + 200L
            )
        )

        assertThat(entering.state).isEqualTo(OverlayVisibilityStateMachine.State.ENTERING_SELECTED)
        assertThat(stillEntering.state).isEqualTo(OverlayVisibilityStateMachine.State.VISIBLE)
        assertThat(stillEntering.overlayVisible).isTrue()
        assertThat(visible.state).isEqualTo(OverlayVisibilityStateMachine.State.VISIBLE)
        assertThat(visible.overlayVisible).isTrue()
    }

    private fun snapshot(
        nowMs: Long,
        resolved: String?,
        root: String?,
        event: String?,
        eventTimestampMs: Long?,
        appUiInForeground: Boolean = false
    ): OverlayVisibilityStateMachine.Snapshot {
        return OverlayVisibilityStateMachine.Snapshot(
            accessibilityEnabled = true,
            selectedApps = setOf("de.robnice.homeshoplist"),
            ownPackageName = "de.robnice.navxs",
            appUiInForeground = appUiInForeground,
            resolvedForegroundPackage = resolved,
            rootPackage = root,
            eventPackage = event,
            eventTimestampMs = eventTimestampMs,
            nowMs = nowMs
        )
    }
}
