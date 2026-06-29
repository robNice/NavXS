package de.robnice.navxs.accessibility

class OverlayVisibilityStateMachine(
    private val enterDelayMs: Long = 100L,
    private val leaveDelayMs: Long = 175L,
    private val verificationIntervalMs: Long = 100L,
    private val enterVerificationMaxMs: Long = 800L,
    private val leaveVerificationMaxMs: Long = 1_200L,
    private val eventFreshMs: Long = 300L
) {
    private var state: State = State.HIDDEN
    private var trackedPackage: String? = null
    private var stateDeadlineMs: Long? = null
    private var enteringStartedMs: Long? = null
    private var leavingStartedMs: Long? = null

    fun evaluate(snapshot: Snapshot): Decision {
        val signals = Signals.from(snapshot, eventFreshMs)
        val selectedCandidate = signals.selectedCandidate(snapshot.selectedApps)
        val selectedConfirmationCount = selectedCandidate?.let { signals.confirmationCount(it) } ?: 0
        val shouldForceHidden = snapshot.selectedApps.isEmpty() ||
            !snapshot.accessibilityEnabled ||
            signals.navXsUiVisible(snapshot.ownPackageName, snapshot.appUiInForeground)

        if (shouldForceHidden) {
            state = State.HIDDEN
            trackedPackage = null
            stateDeadlineMs = null
            enteringStartedMs = null
            leavingStartedMs = null
            return Decision(state = state, overlayVisible = false)
        }

        return when (state) {
            State.HIDDEN -> {
                if (selectedCandidate != null) {
                    state = State.ENTERING_SELECTED
                    trackedPackage = selectedCandidate
                    enteringStartedMs = snapshot.nowMs
                    stateDeadlineMs = snapshot.nowMs + enterDelayMs
                    leavingStartedMs = null
                    Decision(
                        state = state,
                        overlayVisible = false,
                        trackedPackage = trackedPackage,
                        reevaluateAfterMs = enterDelayMs
                    )
                } else {
                    Decision(state = state, overlayVisible = false)
                }
            }

            State.ENTERING_SELECTED -> {
                val enteringPackage = trackedPackage
                val canKeepVerifyingEnter = enteringPackage != null &&
                    signals.supportsPackage(enteringPackage) &&
                    snapshot.nowMs - (enteringStartedMs ?: snapshot.nowMs) <= enterVerificationMaxMs
                when {
                    selectedCandidate == null -> {
                        state = State.HIDDEN
                        trackedPackage = null
                        stateDeadlineMs = null
                        enteringStartedMs = null
                        Decision(state = state, overlayVisible = false)
                    }

                    selectedCandidate != trackedPackage -> {
                        trackedPackage = selectedCandidate
                        enteringStartedMs = snapshot.nowMs
                        stateDeadlineMs = snapshot.nowMs + enterDelayMs
                        leavingStartedMs = null
                        Decision(
                            state = state,
                            overlayVisible = false,
                            trackedPackage = trackedPackage,
                            reevaluateAfterMs = enterDelayMs
                        )
                    }

                    snapshot.nowMs < (stateDeadlineMs ?: snapshot.nowMs) -> {
                        Decision(
                            state = state,
                            overlayVisible = false,
                            trackedPackage = trackedPackage,
                            reevaluateAfterMs = (stateDeadlineMs ?: snapshot.nowMs) - snapshot.nowMs
                        )
                    }

                    selectedConfirmationCount >= 2 -> {
                        state = State.VISIBLE
                        stateDeadlineMs = null
                        enteringStartedMs = null
                        leavingStartedMs = null
                        Decision(state = state, overlayVisible = true, trackedPackage = trackedPackage)
                    }

                    canKeepVerifyingEnter -> {
                        stateDeadlineMs = snapshot.nowMs + verificationIntervalMs
                        Decision(
                            state = state,
                            overlayVisible = false,
                            trackedPackage = trackedPackage,
                            reevaluateAfterMs = verificationIntervalMs
                        )
                    }

                    else -> {
                        state = State.HIDDEN
                        trackedPackage = null
                        stateDeadlineMs = null
                        enteringStartedMs = null
                        Decision(state = state, overlayVisible = false)
                    }
                }
            }

            State.VISIBLE -> {
                val visiblePackage = trackedPackage
                if (visiblePackage != null && signals.supportsPackage(visiblePackage)) {
                    Decision(state = state, overlayVisible = true, trackedPackage = visiblePackage)
                } else {
                    state = State.LEAVING_APP
                    stateDeadlineMs = snapshot.nowMs + leaveDelayMs
                    enteringStartedMs = null
                    leavingStartedMs = snapshot.nowMs
                    Decision(
                        state = state,
                        overlayVisible = true,
                        trackedPackage = trackedPackage,
                        reevaluateAfterMs = leaveDelayMs
                    )
                }
            }

            State.LEAVING_APP -> {
                val leavingPackage = trackedPackage
                when {
                    leavingPackage != null && signals.supportsPackage(leavingPackage) -> {
                        state = State.VISIBLE
                        stateDeadlineMs = null
                        enteringStartedMs = null
                        leavingStartedMs = null
                        Decision(state = state, overlayVisible = true, trackedPackage = leavingPackage)
                    }

                    snapshot.nowMs < (stateDeadlineMs ?: snapshot.nowMs) -> {
                        Decision(
                            state = state,
                            overlayVisible = true,
                            trackedPackage = trackedPackage,
                            reevaluateAfterMs = (stateDeadlineMs ?: snapshot.nowMs) - snapshot.nowMs
                        )
                    }

                    leavingPackage != null &&
                        signals.supportsFurtherVerification(leavingPackage) &&
                        (
                            signals.rootStillSupportsPackage(leavingPackage) ||
                                snapshot.nowMs - (leavingStartedMs ?: snapshot.nowMs) <= leaveVerificationMaxMs
                            ) -> {
                        stateDeadlineMs = snapshot.nowMs + verificationIntervalMs
                        Decision(
                            state = state,
                            overlayVisible = true,
                            trackedPackage = trackedPackage,
                            reevaluateAfterMs = verificationIntervalMs
                        )
                    }

                    else -> {
                        state = State.HIDDEN
                        trackedPackage = null
                        stateDeadlineMs = null
                        enteringStartedMs = null
                        leavingStartedMs = null
                        Decision(state = state, overlayVisible = false)
                    }
                }
            }
        }
    }

    data class Snapshot(
        val accessibilityEnabled: Boolean,
        val selectedApps: Set<String>,
        val ownPackageName: String,
        val appUiInForeground: Boolean,
        val resolvedForegroundPackage: String?,
        val rootPackage: String?,
        val eventPackage: String?,
        val eventTimestampMs: Long?,
        val nowMs: Long
    )

    data class Decision(
        val state: State,
        val overlayVisible: Boolean,
        val trackedPackage: String? = null,
        val reevaluateAfterMs: Long? = null
    )

    enum class State {
        HIDDEN,
        ENTERING_SELECTED,
        VISIBLE,
        LEAVING_APP
    }

    private data class Signals(
        val resolvedPackage: String?,
        val rootPackage: String?,
        val eventPackage: String?
    ) {
        fun selectedCandidate(selectedApps: Set<String>): String? {
            return when {
                resolvedPackage in selectedApps -> resolvedPackage
                eventPackage in selectedApps -> eventPackage
                rootPackage in selectedApps && rootPackage != null && !hasConflictingForeground(rootPackage) -> rootPackage
                else -> null
            }
        }

        fun confirmationCount(packageName: String): Int {
            var count = 0
            if (resolvedPackage == packageName) count++
            if (eventPackage == packageName) count++
            if (rootPackage == packageName) count++
            return count
        }

        fun supportsPackage(packageName: String): Boolean {
            return resolvedPackage == packageName ||
                eventPackage == packageName ||
                (rootPackage == packageName && !hasConflictingForeground(packageName))
        }

        fun supportsFurtherVerification(packageName: String): Boolean {
            return rootPackage == null || rootPackage == packageName
        }

        fun rootStillSupportsPackage(packageName: String): Boolean {
            return rootPackage == packageName
        }

        private fun hasConflictingForeground(packageName: String): Boolean {
            if (!resolvedPackage.isNullOrBlank() && resolvedPackage != packageName) return true
            if (!eventPackage.isNullOrBlank() && eventPackage != packageName) return true
            return false
        }

        fun navXsUiVisible(ownPackageName: String, appUiInForeground: Boolean): Boolean {
            if (!appUiInForeground) return false
            return resolvedPackage == ownPackageName ||
                eventPackage == ownPackageName ||
                rootPackage == ownPackageName
        }

        companion object {
            fun from(snapshot: Snapshot, eventFreshMs: Long): Signals {
                val eventIsFresh = snapshot.eventPackage != null &&
                    snapshot.eventTimestampMs != null &&
                    snapshot.nowMs - snapshot.eventTimestampMs <= eventFreshMs
                return Signals(
                    resolvedPackage = snapshot.resolvedForegroundPackage,
                    rootPackage = snapshot.rootPackage,
                    eventPackage = if (eventIsFresh) snapshot.eventPackage else null
                )
            }
        }
    }
}
