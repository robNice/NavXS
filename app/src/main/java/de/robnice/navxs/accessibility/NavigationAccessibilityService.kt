package de.robnice.navxs.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import de.robnice.navxs.R
import de.robnice.navxs.data.SettingsRepository
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.overlay.OverlayController
import de.robnice.navxs.overlay.OverlayViewport
import de.robnice.navxs.ui.AppForegroundState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NavigationAccessibilityService : AccessibilityService() {
    private var scope = createServiceScope()
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var overlayController: OverlayController
    private lateinit var foregroundResolver: ForegroundPackageResolver
    private val visibilityStateMachine = OverlayVisibilityStateMachine()

    private var currentForegroundPackage: String? = null
    private var latestSettings = de.robnice.navxs.data.NavDefaults.defaultOverlaySettings()
    private var selectedApps: Set<String> = emptySet()
    private var appUiInForeground: Boolean = false
    private var pendingVisibilityJob: Job? = null
    private var lastPressId: Long? = null
    private var lastPressStartedUptimeMs: Long = 0L
    private var lastActionType: NavButtonType? = null
    private var lastActionUptimeMs: Long = 0L
    private var recentsTrace: RecentsTrace? = null
    private var lastEventPackage: String? = null
    private var lastEventTimestampMs: Long = 0L
    private var serviceConnected = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        scope.cancel()
        scope = createServiceScope()
        serviceConnected = true
        settingsRepository = SettingsRepository.create(this)
        overlayController = OverlayController(this, ::performAction)
        foregroundResolver = ForegroundPackageResolver(packageName, IgnoredForegroundPackages)
        scope.launch {
            combine(
                settingsRepository.settingsFlow,
                settingsRepository.selectedAppsFlow,
                AppForegroundState.isInForeground
            ) { settings, apps, isInForeground ->
                Triple(settings, apps, isInForeground)
            }.collect { (settings, apps, isInForeground) ->
                latestSettings = settings
                selectedApps = apps
                appUiInForeground = isInForeground
                updateOverlay()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventType = event?.eventType ?: return
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }
        val nextPackage = event.packageName?.toString()
        val rootPackage = rootInActiveWindow?.packageName?.toString()
        if (!nextPackage.isNullOrBlank() &&
            nextPackage !in IgnoredForegroundPackages &&
            !shouldIgnoreOwnPackageEvent(nextPackage, rootPackage)
        ) {
            lastEventPackage = nextPackage
            lastEventTimestampMs = SystemClock.uptimeMillis()
            currentForegroundPackage = foregroundResolver.onAccessibilityPackage(
                packageName = nextPackage,
                appUiInForeground = appUiInForeground,
                nowMs = lastEventTimestampMs
            )
            Log.d(
                TAG,
                "foregroundPackage=$currentForegroundPackage eventType=$eventType pressId=$lastPressId sincePressMs=${elapsedSinceLastPress()}"
            )
        } else if (!nextPackage.isNullOrBlank()) {
            Log.d(
                TAG,
                "ignoredForegroundPackage=$nextPackage eventType=$eventType rootPackage=$rootPackage appUiInForeground=$appUiInForeground pressId=$lastPressId sincePressMs=${elapsedSinceLastPress()}"
            )
            if (shouldIgnoreOwnPackageEvent(nextPackage, rootPackage)) {
                return
            }
        }
        logRecentsTraceEvent(
            stage = "a11yEvent",
            detail = "eventType=$eventType nextPackage=$nextPackage currentForeground=$currentForegroundPackage appUiInForeground=$appUiInForeground"
        )
        updateOverlay()
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        disconnectService()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        disconnectService()
        super.onDestroy()
    }

    private fun disconnectService() {
        serviceConnected = false
        pendingVisibilityJob?.cancel()
        pendingVisibilityJob = null
        scope.cancel()
        if (::overlayController.isInitialized) {
            overlayController.hide()
        }
    }

    private fun updateOverlay() {
        if (!serviceConnected) {
            Log.d(TAG, "updateOverlay skipped: service disconnected")
            return
        }
        pendingVisibilityJob?.cancel()
        pendingVisibilityJob = null

        val rootPackage = rootInActiveWindow?.packageName?.toString()
        val resolvedForegroundPackage = resolveForegroundPackage()
        val nowMs = SystemClock.uptimeMillis()
        val decision = visibilityStateMachine.evaluate(
            OverlayVisibilityStateMachine.Snapshot(
                accessibilityEnabled = true,
                selectedApps = selectedApps,
                ownPackageName = packageName,
                appUiInForeground = appUiInForeground,
                resolvedForegroundPackage = resolvedForegroundPackage,
                rootPackage = rootPackage,
                eventPackage = lastEventPackage,
                eventTimestampMs = lastEventTimestampMs.takeIf { it > 0L },
                nowMs = nowMs
            )
        )
        val trackedPackage = decision.trackedPackage ?: resolvedForegroundPackage ?: rootPackage
        val baseShouldShow = decision.overlayVisible
        val isWindowed = baseShouldShow && isForegroundAppWindowed()
        val shouldShow = baseShouldShow && !isWindowed

        Log.d(
            TAG,
            "updateOverlay state=${decision.state} package=$trackedPackage resolved=$resolvedForegroundPackage rootPackage=$rootPackage eventPackage=$lastEventPackage selected=${selectedApps.contains(trackedPackage)} appUiInForeground=$appUiInForeground shouldShow=$shouldShow baseShouldShow=$baseShouldShow isWindowed=$isWindowed reevaluateAfterMs=${decision.reevaluateAfterMs} pressId=$lastPressId sincePressMs=${elapsedSinceLastPress()}"
        )
        logRecentsTraceEvent(
            stage = "updateOverlay",
            detail = "state=${decision.state} rootPackage=$rootPackage foregroundPackage=$trackedPackage shouldShow=$shouldShow selected=${selectedApps.contains(trackedPackage)}"
        )
        logRecentsTraceWindows("updateOverlay")

        if (shouldShow) {
            overlayController.show(latestSettings)
        } else {
            overlayController.hide()
        }

        decision.reevaluateAfterMs?.takeIf { it > 0L }?.let { delayMs ->
            Log.d(TAG, "updateOverlay scheduleReevaluate delayMs=$delayMs state=${decision.state}")
            pendingVisibilityJob = scope.launch {
                delay(delayMs)
                pendingVisibilityJob = null
                updateOverlay()
            }
        }
    }

    private fun performAction(type: NavButtonType, pressId: Long) {
        lastPressId = pressId
        lastPressStartedUptimeMs = SystemClock.uptimeMillis()
        lastActionType = type
        lastActionUptimeMs = lastPressStartedUptimeMs
        Log.d(TAG, "performActionStart pressId=$pressId type=$type foregroundPackage=$currentForegroundPackage")
        if (type == NavButtonType.RECENTS) {
            recentsTrace = RecentsTrace(
                pressId = pressId,
                originPackage = currentForegroundPackage,
                startedUptimeMs = lastPressStartedUptimeMs
            )
            logRecentsTraceEvent(
                stage = "performActionStart",
                detail = "originPackage=$currentForegroundPackage"
            )
        }
        val action = when (type) {
            NavButtonType.BACK -> GLOBAL_ACTION_BACK
            NavButtonType.HOME -> GLOBAL_ACTION_HOME
            NavButtonType.RECENTS -> GLOBAL_ACTION_RECENTS
        }
        val success = performGlobalAction(action)
        Log.d(TAG, "performActionResult pressId=$pressId type=$type success=$success durationMs=${elapsedSinceLastPress()}")
        if (type == NavButtonType.RECENTS) {
            logRecentsTraceEvent(
                stage = "performActionResult",
                detail = "success=$success durationMs=${elapsedSinceLastPress()}"
            )
        }
        if (!success) {
            android.widget.Toast.makeText(this, R.string.error_action_failed, android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (type == NavButtonType.RECENTS || type == NavButtonType.HOME) {
            scope.launch {
                delay(ActionResyncDelayMs)
                val resyncedPackage = resolveForegroundPackage()
                Log.d(
                    TAG,
                    "postActionResync pressId=$pressId type=$type package=$resyncedPackage sincePressMs=${elapsedSinceLastPress()}"
                )
                if (type == NavButtonType.RECENTS) {
                    logRecentsTraceEvent(
                        stage = "postActionResync",
                        detail = "package=$resyncedPackage"
                    )
                    logRecentsTraceWindows("postActionResync")
                }
                updateOverlay()
            }
        }
    }

    private fun resolveForegroundPackage(): String? {
        val rootPackage = rootInActiveWindow?.packageName?.toString()
        val resolved = foregroundResolver.resolveFromRoot(
            rootPackage = rootPackage,
            appUiInForeground = appUiInForeground,
            nowMs = SystemClock.uptimeMillis()
        )
        if (resolved != currentForegroundPackage) {
            currentForegroundPackage = resolved
            Log.d(TAG, "resolvedForegroundPackage=$resolved rootPackage=$rootPackage")
            logRecentsTraceEvent(
                stage = "resolveForegroundPackage",
                detail = "rootPackage=$rootPackage resolved=$resolved"
            )
        }
        return resolved
    }

    private fun shouldIgnoreOwnPackageEvent(
        eventPackage: String,
        rootPackage: String?
    ): Boolean {
        if (eventPackage != packageName) return false
        if (!appUiInForeground) return true
        return rootPackage != packageName
    }

    private fun elapsedSinceLastPress(): Long? {
        if (lastPressStartedUptimeMs == 0L) return null
        return SystemClock.uptimeMillis() - lastPressStartedUptimeMs
    }

    private fun logRecentsTraceEvent(stage: String, detail: String) {
        val trace = recentsTrace ?: return
        val elapsedMs = SystemClock.uptimeMillis() - trace.startedUptimeMs
        if (elapsedMs > RecentsTraceWindowMs) {
            recentsTrace = null
            return
        }
        Log.d(
            TAG,
            "recentsTrace pressId=${trace.pressId} origin=${trace.originPackage} elapsedMs=$elapsedMs stage=$stage $detail"
        )
    }

    private fun logRecentsTraceWindows(stage: String) {
        val trace = recentsTrace ?: return
        val elapsedMs = SystemClock.uptimeMillis() - trace.startedUptimeMs
        if (elapsedMs > RecentsTraceWindowMs) return

        val root = rootInActiveWindow
        val rootSummary = "root=${root?.packageName}/${root?.className}"
        val windowsSummary = runCatching {
            windows
                .take(4)
                .joinToString(" | ") { window ->
                    describeWindow(window)
                }
        }.getOrElse { error ->
            "windowsError=${error::class.java.simpleName}"
        }
        Log.d(
            TAG,
            "recentsTrace pressId=${trace.pressId} origin=${trace.originPackage} elapsedMs=$elapsedMs stage=$stage windows $rootSummary $windowsSummary"
        )
    }

    private fun describeWindow(window: AccessibilityWindowInfo): String {
        val root = window.root
        return buildString {
            append("type=")
            append(window.type)
            append(",focused=")
            append(window.isFocused)
            append(",active=")
            append(window.isActive)
            append(",pkg=")
            append(root?.packageName)
            append(",cls=")
            append(root?.className)
        }
    }

    private fun isForegroundAppWindowed(): Boolean {
        val appWindows = try {
            windows.filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
        } catch (e: Exception) {
            Log.d(TAG, "isForegroundAppWindowed windowsError=${e::class.java.simpleName}")
            return false
        }
        val activeWindow = appWindows.firstOrNull { it.isActive } ?: return false
        val bounds = Rect()
        activeWindow.getBoundsInScreen(bounds)
        val displayMetrics = OverlayViewport.metrics(this)
        val displayWidth = displayMetrics.widthPixels
        val displayHeight = displayMetrics.heightPixels
        val isWindowed = bounds.width() < displayWidth * 0.95f || bounds.height() < displayHeight * 0.75f
        Log.d(TAG, "isForegroundAppWindowed=$isWindowed bounds=$bounds display=${displayWidth}x${displayHeight}")
        return isWindowed
    }

    private data class RecentsTrace(
        val pressId: Long,
        val originPackage: String?,
        val startedUptimeMs: Long
    )

    private companion object {
        const val TAG = "NavXsA11yService"
        const val ActionResyncDelayMs = 700L
        const val RecentsTraceWindowMs = 2_500L
        val IgnoredForegroundPackages = setOf(
            "com.android.systemui",
            "com.google.android.inputmethod.latin",
            "com.android.inputmethod.latin"
        )
    }
}

private fun createServiceScope(): CoroutineScope =
    CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
