package de.robnice.navxs.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityWindowInfo
import android.view.accessibility.AccessibilityEvent
import de.robnice.navxs.R
import de.robnice.navxs.data.SettingsRepository
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.domain.OverlayVisibilityPolicy
import de.robnice.navxs.overlay.OverlayController
import de.robnice.navxs.ui.AppForegroundState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NavigationAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var overlayController: OverlayController
    private lateinit var visibilityPolicy: OverlayVisibilityPolicy
    private lateinit var foregroundResolver: ForegroundPackageResolver
    private var currentForegroundPackage: String? = null
    private var latestSettings = de.robnice.navxs.data.NavDefaults.defaultOverlaySettings()
    private var selectedApps: Set<String> = emptySet()
    private var appUiInForeground: Boolean = false
    private var pendingHideJob: Job? = null
    private var lastPressId: Long? = null
    private var lastPressStartedUptimeMs: Long = 0L
    private var lastActionType: NavButtonType? = null
    private var lastActionUptimeMs: Long = 0L
    private var recentsTrace: RecentsTrace? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        settingsRepository = SettingsRepository.create(this)
        overlayController = OverlayController(this, ::performAction)
        visibilityPolicy = OverlayVisibilityPolicy(packageName)
        foregroundResolver = ForegroundPackageResolver(packageName, IgnoredForegroundPackages)
        scope.launch {
            combine(
                settingsRepository.settingsFlow,
                settingsRepository.selectedAppsFlow,
                AppForegroundState.isInForeground
            ) { settings, apps, isInForeground -> Triple(settings, apps, isInForeground) }.collect { (settings, apps, isInForeground) ->
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
        if (!nextPackage.isNullOrBlank() &&
            nextPackage !in IgnoredForegroundPackages &&
            !(nextPackage == packageName && !appUiInForeground)
        ) {
            currentForegroundPackage = foregroundResolver.onAccessibilityPackage(
                packageName = nextPackage,
                appUiInForeground = appUiInForeground,
                nowMs = SystemClock.uptimeMillis()
            )
            Log.d(
                TAG,
                "foregroundPackage=$currentForegroundPackage eventType=$eventType pressId=$lastPressId sincePressMs=${elapsedSinceLastPress()}"
            )
        } else if (!nextPackage.isNullOrBlank()) {
            Log.d(
                TAG,
                "ignoredForegroundPackage=$nextPackage eventType=$eventType appUiInForeground=$appUiInForeground pressId=$lastPressId sincePressMs=${elapsedSinceLastPress()}"
            )
            if (nextPackage == packageName && !appUiInForeground) {
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

    override fun onDestroy() {
        pendingHideJob?.cancel()
        overlayController.hide()
        super.onDestroy()
    }

    private fun updateOverlay() {
        val rootPackage = rootInActiveWindow?.packageName?.toString()
        val foregroundPackage = resolveForegroundPackage()
        val baseShouldShow = visibilityPolicy.shouldShowOverlay(
            accessibilityEnabled = true,
            foregroundPackageName = foregroundPackage,
            selectedApps = selectedApps,
            appUiInForeground = appUiInForeground
        )
        val isWindowed = baseShouldShow && isForegroundAppWindowed()
        val shouldShow = baseShouldShow && !isWindowed
        Log.d(
            TAG,
            "updateOverlay package=$foregroundPackage selected=${selectedApps.contains(foregroundPackage)} appUiInForeground=$appUiInForeground shouldShow=$shouldShow baseShouldShow=$baseShouldShow isWindowed=$isWindowed pressId=$lastPressId sincePressMs=${elapsedSinceLastPress()}"
        )
        logRecentsTraceEvent(
            stage = "updateOverlay",
            detail = "rootPackage=$rootPackage foregroundPackage=$foregroundPackage shouldShow=$shouldShow selected=${selectedApps.contains(foregroundPackage)}"
        )
        logRecentsTraceWindows("updateOverlay")
        if (shouldShow) {
            pendingHideJob?.cancel()
            pendingHideJob = null
            overlayController.show(latestSettings)
        } else {
            if (pendingHideJob?.isActive == true) {
                Log.d(TAG, "updateOverlay hidePendingAlready pressId=$lastPressId")
                return
            }
            Log.d(TAG, "updateOverlay scheduleHide delayMs=$TransientHideDelayMs pressId=$lastPressId")
            pendingHideJob = scope.launch {
                delay(TransientHideDelayMs)
                val foregroundAfterDelay = resolveForegroundPackage()
                val stillShouldHide = !visibilityPolicy.shouldShowOverlay(
                    accessibilityEnabled = true,
                    foregroundPackageName = foregroundAfterDelay,
                    selectedApps = selectedApps,
                    appUiInForeground = appUiInForeground
                )
                val shouldDeferHide = shouldDeferHideForTransientLauncher(foregroundAfterDelay)
                Log.d(
                    TAG,
                    "updateOverlay hideCheck package=$foregroundAfterDelay stillShouldHide=$stillShouldHide shouldDeferHide=$shouldDeferHide pressId=$lastPressId sincePressMs=${elapsedSinceLastPress()}"
                )
                logRecentsTraceEvent(
                    stage = "hideCheck",
                    detail = "foregroundAfterDelay=$foregroundAfterDelay stillShouldHide=$stillShouldHide shouldDeferHide=$shouldDeferHide"
                )
                if (stillShouldHide && !shouldDeferHide) {
                    overlayController.hide()
                } else if (stillShouldHide) {
                    Log.d(TAG, "updateOverlay hideDeferred package=$foregroundAfterDelay action=$lastActionType")
                }
                pendingHideJob = null
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

    private fun elapsedSinceLastPress(): Long? {
        if (lastPressStartedUptimeMs == 0L) return null
        return SystemClock.uptimeMillis() - lastPressStartedUptimeMs
    }

    private fun shouldDeferHideForTransientLauncher(foregroundPackage: String?): Boolean {
        if (foregroundPackage != LauncherPackageName) return false
        if (lastActionType != NavButtonType.RECENTS && lastActionType != NavButtonType.HOME) return false
        val elapsedSinceAction = SystemClock.uptimeMillis() - lastActionUptimeMs
        return elapsedSinceAction <= LauncherHideGraceMs
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
        val displayWidth = resources.displayMetrics.widthPixels
        val displayHeight = resources.displayMetrics.heightPixels
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
        const val TransientHideDelayMs = 350L
        const val ActionResyncDelayMs = 700L
        const val RecentsTraceWindowMs = 2_500L
        const val LauncherHideGraceMs = 1_800L
        const val LauncherPackageName = "com.google.android.apps.nexuslauncher"
        val IgnoredForegroundPackages = setOf(
            "com.android.systemui",
            "com.google.android.inputmethod.latin",
            "com.android.inputmethod.latin"
        )
    }
}
