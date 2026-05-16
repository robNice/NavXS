package de.robnice.navxs.ui

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.robnice.navxs.R
import de.robnice.navxs.accessibility.ForegroundAppDetector
import de.robnice.navxs.accessibility.NavigationAccessibilityService
import de.robnice.navxs.data.InstalledAppsRepository
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.SettingsRepository
import de.robnice.navxs.data.models.InstalledAppInfo
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlaySettings
import de.robnice.navxs.domain.ButtonSettingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

data class MainUiState(
    val accessibilityCheckComplete: Boolean = false,
    val accessibilityEnabled: Boolean = false,
    val settings: OverlaySettings = NavDefaults.defaultOverlaySettings(),
    val selectedApps: Set<String> = emptySet(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val selectedTabIndex: Int = 0,
    val showSystemApps: Boolean = false,
    val searchQuery: String = "",
    val precisionDialogOpen: Boolean = false,
    val message: String? = null
)

private data class UiStateBaseInput(
    val accessibilityCheckComplete: Boolean,
    val accessibilityEnabled: Boolean,
    val settings: OverlaySettings,
    val selectedApps: Set<String>,
    val selectedTabIndex: Int,
    val showSystemApps: Boolean,
    val searchQuery: String,
    val precisionDialogOpen: Boolean
)

private data class AccessibilitySettingsState(
    val accessibilityCheckComplete: Boolean,
    val accessibilityEnabled: Boolean,
    val settings: OverlaySettings
)

private data class AccessibilitySelectionState(
    val accessibilityCheckComplete: Boolean,
    val accessibilityEnabled: Boolean,
    val settings: OverlaySettings,
    val selectedApps: Set<String>
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository.create(application)
    private val installedAppsRepository = InstalledAppsRepository(application)
    private val detector = ForegroundAppDetector(application)
    private val buttonSettingsUseCase = ButtonSettingsUseCase()
    private val searchQuery = MutableStateFlow("")
    private val editMode = MutableStateFlow(false)
    private val precisionDialogOpen = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)
    private val accessibilityEnabled = MutableStateFlow(false)
    private val accessibilityCheckComplete = MutableStateFlow(false)

    init {
        refreshAccessibilityStatus()
    }

    private val persistedSettingsFlow: Flow<OverlaySettings> =
        settingsRepository.settingsFlow.combine(editMode) { settings, isEditMode ->
            settings.copy(editMode = isEditMode)
        }

    private val installedAppsBaseFlow: Flow<List<InstalledAppInfo>> =
        settingsRepository.showSystemAppsFlow.map { showSystemApps ->
            installedAppsRepository.loadApps(showSystemApps)
        }

    private val installedAppsFlow: Flow<List<InstalledAppInfo>> = combine(
        installedAppsBaseFlow,
        settingsRepository.selectedAppsFlow,
        searchQuery
    ) { installedApps, selectedApps, query ->
        installedApps
            .map { app -> app.copy(enabled = app.packageName in selectedApps) }
            .filter {
                query.isBlank() ||
                    it.appName.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
            .sortedWith(compareByDescending<InstalledAppInfo> { it.enabled }.thenBy { it.appName.lowercase() })
    }

    private val uiStateBaseFlow: Flow<MainUiState> =
        combine(accessibilityCheckComplete, accessibilityEnabled, persistedSettingsFlow) { checkComplete, accessibility, settings ->
            AccessibilitySettingsState(
                accessibilityCheckComplete = checkComplete,
                accessibilityEnabled = accessibility,
                settings = settings
            )
        }.combine(settingsRepository.selectedAppsFlow) { base, selectedApps ->
            AccessibilitySelectionState(
                accessibilityCheckComplete = base.accessibilityCheckComplete,
                accessibilityEnabled = base.accessibilityEnabled,
                settings = base.settings,
                selectedApps = selectedApps
            )
        }.combine(settingsRepository.selectedTabFlow) { base, selectedTabIndex ->
            UiStateBaseInput(
                accessibilityCheckComplete = base.accessibilityCheckComplete,
                accessibilityEnabled = base.accessibilityEnabled,
                settings = base.settings,
                selectedApps = base.selectedApps,
                selectedTabIndex = selectedTabIndex,
                showSystemApps = false,
                searchQuery = "",
                precisionDialogOpen = false
            )
        }.combine(settingsRepository.showSystemAppsFlow) { base, showSystemApps ->
            base.copy(showSystemApps = showSystemApps)
        }.combine(searchQuery) { base, query ->
            base.copy(searchQuery = query)
        }.combine(precisionDialogOpen) { base, precisionOpen ->
            base.copy(precisionDialogOpen = precisionOpen)
        }.combine(message) { base, currentMessage ->
            MainUiState(
                accessibilityCheckComplete = base.accessibilityCheckComplete,
                accessibilityEnabled = base.accessibilityEnabled,
                settings = base.settings,
                selectedApps = base.selectedApps,
                selectedTabIndex = base.selectedTabIndex,
                showSystemApps = base.showSystemApps,
                searchQuery = base.searchQuery,
                precisionDialogOpen = base.precisionDialogOpen,
                message = currentMessage
            )
        }

    val uiState: StateFlow<MainUiState> = combine(uiStateBaseFlow, installedAppsFlow) { state, apps ->
        state.copy(installedApps = apps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState(accessibilityCheckComplete = false))

    fun refreshAccessibilityStatus() {
        viewModelScope.launch {
            accessibilityCheckComplete.value = false
            yield()
            accessibilityEnabled.value = checkAccessibility()
            accessibilityCheckComplete.value = true
        }
    }

    fun openAccessibilitySettingsIntent(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun openAccessibilitySettings() {
        accessibilityCheckComplete.value = false
        getApplication<Application>().startActivity(openAccessibilitySettingsIntent())
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setShowSystemApps(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowSystemApps(show)
        }
    }

    fun setSelectedTab(index: Int) {
        viewModelScope.launch {
            settingsRepository.setSelectedTab(index)
        }
    }

    fun setSelectedButton(type: NavButtonType) {
        persist(buttonSettingsUseCase.selectButton(uiState.value.settings, type))
    }

    fun setActive(type: NavButtonType, active: Boolean) {
        val result = buttonSettingsUseCase.setActive(uiState.value.settings, type, active)
        result.onSuccess(::persist)
        result.onFailure {
            message.value = getApplication<Application>().getString(R.string.error_last_active_button)
        }
    }

    fun setColor(type: NavButtonType, colorArgb: Long) =
        persist(buttonSettingsUseCase.setColor(uiState.value.settings, type, colorArgb))

    fun setOpacity(type: NavButtonType, opacity: Float) =
        persist(buttonSettingsUseCase.setOpacity(uiState.value.settings, type, opacity))

    fun setSize(type: NavButtonType, sizePercent: Int) =
        persist(buttonSettingsUseCase.setSizePercent(uiState.value.settings, type, sizePercent))

    fun setBackgroundColor(type: NavButtonType, colorArgb: Long) =
        persist(buttonSettingsUseCase.setBackgroundColor(uiState.value.settings, type, colorArgb))

    fun setBackgroundOpacity(type: NavButtonType, opacity: Float) =
        persist(buttonSettingsUseCase.setBackgroundOpacity(uiState.value.settings, type, opacity))

    fun setBackgroundSize(type: NavButtonType, sizePercent: Int) =
        persist(buttonSettingsUseCase.setBackgroundSizePercent(uiState.value.settings, type, sizePercent))

    fun setBackgroundSoftness(type: NavButtonType, softnessPercent: Int) =
        persist(buttonSettingsUseCase.setBackgroundSoftnessPercent(uiState.value.settings, type, softnessPercent))

    fun setTheme(type: NavButtonType, themeId: String) =
        persist(buttonSettingsUseCase.setTheme(uiState.value.settings, type, themeId))

    fun setEditMode(editMode: Boolean) {
        this.editMode.value = editMode
    }

    fun openEditMode() {
        setEditMode(true)
    }

    fun closeEditMode() {
        precisionDialogOpen.value = false
        setEditMode(false)
    }

    fun setPrecisionStep(stepPx: Int) =
        persist(buttonSettingsUseCase.setPrecisionStep(uiState.value.settings, stepPx))

    fun moveSelectedButton(deltaX: Float, deltaY: Float) {
        persist(
            buttonSettingsUseCase.moveBy(
                uiState.value.settings,
                uiState.value.settings.selectedButtonType,
                deltaX,
                deltaY
            )
        )
    }

    fun setSelectedButtonPosition(x: Int, y: Int) {
        setButtonPosition(uiState.value.settings.selectedButtonType, x, y)
    }

    fun setButtonPosition(type: NavButtonType, x: Int, y: Int) {
        persist(
            buttonSettingsUseCase.setPosition(
                uiState.value.settings,
                type,
                x,
                y
            )
        )
    }

    fun resetSelectedButtonPosition() {
        persist(
            buttonSettingsUseCase.resetPosition(
                uiState.value.settings,
                uiState.value.settings.selectedButtonType
            )
        )
    }

    fun setButtonPositions(positions: Map<NavButtonType, Pair<Int, Int>>) {
        persist(
            buttonSettingsUseCase.setPositions(
                uiState.value.settings,
                positions
            )
        )
    }

    fun setPrecisionDialogOpen(open: Boolean) {
        precisionDialogOpen.value = if (uiState.value.settings.editMode) open else false
    }

    fun toggleApp(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            val next = uiState.value.selectedApps.toMutableSet()
            if (enabled) next += packageName else next -= packageName
            settingsRepository.saveSelectedApps(next)
        }
    }

    fun consumeMessage() {
        message.value = null
    }

    private fun checkAccessibility(): Boolean {
        return detector.isAccessibilityServiceEnabled(NavigationAccessibilityService::class.java.name)
    }

    private fun persist(settings: OverlaySettings) {
        viewModelScope.launch {
            settingsRepository.saveSettings(settings.copy(editMode = false))
        }
    }
}
