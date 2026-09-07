package de.robnice.navxs.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.robnice.navxs.R
import de.robnice.navxs.accessibility.ForegroundAppDetector
import de.robnice.navxs.accessibility.NavigationAccessibilityService
import de.robnice.navxs.data.InstalledAppsRepository
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.data.SettingsRepository
import de.robnice.navxs.data.models.AppOverlayConfiguration
import de.robnice.navxs.data.models.AppThemeMode
import de.robnice.navxs.data.models.InstalledAppInfo
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlaySettings
import de.robnice.navxs.domain.ButtonSettingsUseCase
import de.robnice.navxs.domain.OverlayConfigurationResolver
import de.robnice.navxs.overlay.OverlayViewport
import de.robnice.navxs.ui.settings.PositionBackgroundImageLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield

data class MainUiState(
    val accessibilityCheckComplete: Boolean = false,
    val accessibilityEnabled: Boolean = false,
    val appsLoading: Boolean = false,
    val settings: OverlaySettings = NavDefaults.defaultOverlaySettings(),
    val defaultSettings: OverlaySettings = NavDefaults.defaultOverlaySettings(),
    val appConfigurations: Map<String, AppOverlayConfiguration> = emptyMap(),
    val selectedConfigurationPackage: String? = null,
    val selectedApps: Set<String> = emptySet(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val selectedTabIndex: Int = 0,
    val showSystemApps: Boolean = false,
    val searchQuery: String = "",
    val precisionDialogOpen: Boolean = false,
    val positionBackgroundDialogOpen: Boolean = false,
    val positionBackgroundUri: String? = null,
    val positionBackgroundAlpha: Int = SettingsRepository.DefaultPositionBackgroundAlpha,
    val appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val burnInProtectionEnabled: Boolean = false,
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
    val precisionDialogOpen: Boolean,
    val positionBackgroundDialogOpen: Boolean,
    val positionBackgroundUri: String?,
    val positionBackgroundAlpha: Int,
    val appsLoading: Boolean
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

private data class DesignConfigurationState(
    val configurations: Map<String, AppOverlayConfiguration>,
    val selectedPackage: String?,
    val positionBackgroundUri: String?,
    val positionBackgroundAlpha: Int
)

private data class GeneralPreferencesState(
    val appThemeMode: AppThemeMode,
    val burnInProtectionEnabled: Boolean
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository.create(application)
    private val installedAppsRepository = InstalledAppsRepository(application)
    private val detector = ForegroundAppDetector(application)
    private val buttonSettingsUseCase = ButtonSettingsUseCase()
    private val positionBackgroundImageLoader = PositionBackgroundImageLoader(application.contentResolver)
    private val searchQuery = MutableStateFlow("")
    private val editMode = MutableStateFlow(false)
    private val precisionDialogOpen = MutableStateFlow(false)
    private val positionBackgroundDialogOpen = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)
    private val accessibilityEnabled = MutableStateFlow(false)
    private val accessibilityCheckComplete = MutableStateFlow(false)
    private val appsLoading = MutableStateFlow(false)
    private val installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val selectedConfigurationPackage = MutableStateFlow<String?>(null)
    private val appsScanMutex = Mutex()
    private val positionBackgroundMutex = Mutex()
    private var appsScanned = false

    init {
        refreshAccessibilityStatus()
    }

    private val persistedSettingsFlow: Flow<OverlaySettings> =
        settingsRepository.settingsFlow.combine(editMode) { settings, isEditMode ->
            settings.copy(editMode = isEditMode)
        }

    private val installedAppsFlow: Flow<List<InstalledAppInfo>> = combine(
        installedApps,
        settingsRepository.selectedAppsFlow
    ) { apps, selectedApps ->
        apps
            .map { app -> app.copy(enabled = app.packageName in selectedApps) }
            .sortedWith(compareByDescending<InstalledAppInfo> { it.enabled }.thenBy { it.appName.lowercase() })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val designConfigurationFlow: Flow<DesignConfigurationState> =
        selectedConfigurationPackage.flatMapLatest { selectedPackage ->
            combine(
                settingsRepository.appConfigurationsFlow,
                settingsRepository.positionBackgroundUriFlow(selectedPackage),
                settingsRepository.positionBackgroundAlphaFlow(selectedPackage)
            ) { configurations, backgroundUri, backgroundAlpha ->
                DesignConfigurationState(
                    configurations = configurations,
                    selectedPackage = selectedPackage,
                    positionBackgroundUri = backgroundUri,
                    positionBackgroundAlpha = backgroundAlpha
                )
            }
        }

    private val generalPreferencesFlow: Flow<GeneralPreferencesState> = combine(
        settingsRepository.appThemeModeFlow,
        settingsRepository.burnInProtectionEnabledFlow
    ) { appThemeMode, burnInProtectionEnabled ->
        GeneralPreferencesState(appThemeMode, burnInProtectionEnabled)
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
                precisionDialogOpen = false,
                positionBackgroundDialogOpen = false,
                positionBackgroundUri = null,
                positionBackgroundAlpha = SettingsRepository.DefaultPositionBackgroundAlpha,
                appsLoading = false
            )
        }.combine(settingsRepository.showSystemAppsFlow) { base, showSystemApps ->
            base.copy(showSystemApps = showSystemApps)
        }.combine(searchQuery) { base, query ->
            base.copy(searchQuery = query)
        }.combine(precisionDialogOpen) { base, precisionOpen ->
            base.copy(precisionDialogOpen = precisionOpen)
        }.combine(positionBackgroundDialogOpen) { base, backgroundDialogOpen ->
            base.copy(positionBackgroundDialogOpen = backgroundDialogOpen)
        }.combine(appsLoading) { base, isAppsLoading ->
            base.copy(appsLoading = isAppsLoading)
        }.combine(message) { base, currentMessage ->
            MainUiState(
                accessibilityCheckComplete = base.accessibilityCheckComplete,
                accessibilityEnabled = base.accessibilityEnabled,
                appsLoading = base.appsLoading,
                settings = base.settings,
                selectedApps = base.selectedApps,
                selectedTabIndex = base.selectedTabIndex,
                showSystemApps = base.showSystemApps,
                searchQuery = base.searchQuery,
                precisionDialogOpen = base.precisionDialogOpen,
                positionBackgroundDialogOpen = base.positionBackgroundDialogOpen,
                positionBackgroundUri = base.positionBackgroundUri,
                positionBackgroundAlpha = base.positionBackgroundAlpha,
                message = currentMessage
            )
        }

    val uiState: StateFlow<MainUiState> = combine(
        uiStateBaseFlow,
        installedAppsFlow,
        designConfigurationFlow,
        generalPreferencesFlow
    ) { state, apps, designConfiguration, generalPreferences ->
        val validSelectedPackage = designConfiguration.selectedPackage
            ?.takeIf { it in state.selectedApps }
        val effectiveSettings = OverlayConfigurationResolver.resolve(
            defaultSettings = state.settings,
            packageName = validSelectedPackage,
            appConfigurations = designConfiguration.configurations
        )
        state.copy(
            settings = effectiveSettings,
            defaultSettings = state.settings,
            appConfigurations = designConfiguration.configurations,
            selectedConfigurationPackage = validSelectedPackage,
            positionBackgroundUri = designConfiguration.positionBackgroundUri,
            positionBackgroundAlpha = designConfiguration.positionBackgroundAlpha,
            installedApps = apps,
            appThemeMode = generalPreferences.appThemeMode,
            burnInProtectionEnabled = generalPreferences.burnInProtectionEnabled
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        MainUiState(accessibilityCheckComplete = false)
    )

    fun refreshAccessibilityStatus() {
        viewModelScope.launch {
            accessibilityCheckComplete.value = false
            yield()
            val enabled = checkAccessibility()
            accessibilityEnabled.value = enabled
            accessibilityCheckComplete.value = true
            if (enabled) {
                loadInstalledApps()
            } else {
                appsLoading.value = false
            }
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

    fun rescanInstalledApps() {
        if (!accessibilityEnabled.value) return
        viewModelScope.launch {
            loadInstalledApps(force = true)
        }
    }

    fun setSelectedTab(index: Int) {
        viewModelScope.launch {
            settingsRepository.setSelectedTab(index)
        }
    }

    fun selectDesignConfiguration(packageName: String?) {
        selectedConfigurationPackage.value = packageName?.takeIf { it in uiState.value.selectedApps }
    }

    fun setIndividualDesignLayoutEnabled(enabled: Boolean) {
        val packageName = uiState.value.selectedConfigurationPackage ?: return
        viewModelScope.launch {
            settingsRepository.setIndividualAppConfigurationEnabled(packageName, enabled)
        }
    }

    fun copyDesignConfigurationFrom(sourcePackageName: String?) {
        val state = uiState.value
        val targetPackageName = state.selectedConfigurationPackage
        val sourceSettings = if (sourcePackageName == null) {
            state.defaultSettings
        } else {
            val sourceConfiguration = state.appConfigurations[sourcePackageName]
                ?.takeIf { it.individualEnabled }
                ?: return
            state.defaultSettings.copy(buttons = sourceConfiguration.buttons)
        }
        val copiedButtons = OverlayConfigurationResolver.copyButtons(sourceSettings)
        viewModelScope.launch {
            if (targetPackageName == null) {
                settingsRepository.saveSettings(state.defaultSettings.copy(buttons = copiedButtons, editMode = false))
            } else if (state.appConfigurations[targetPackageName]?.individualEnabled == true) {
                settingsRepository.saveAppConfiguration(targetPackageName, copiedButtons)
            }
        }
    }

    fun setSelectedButton(type: NavButtonType) {
        viewModelScope.launch {
            settingsRepository.setSelectedButtonType(type)
        }
    }

    fun setActive(type: NavButtonType, active: Boolean) {
        val result = buttonSettingsUseCase.setActive(uiState.value.settings, type, active)
        result.onSuccess(::persistCurrentConfiguration)
        result.onFailure {
            message.value = getApplication<Application>().getString(R.string.error_last_active_button)
        }
    }

    fun setColor(type: NavButtonType, colorArgb: Long) =
        persistCurrentConfiguration(buttonSettingsUseCase.setColor(uiState.value.settings, type, colorArgb))

    fun setOpacity(type: NavButtonType, opacity: Float) =
        persistCurrentConfiguration(buttonSettingsUseCase.setOpacity(uiState.value.settings, type, opacity))

    fun setSize(type: NavButtonType, sizePercent: Int) =
        persistCurrentConfiguration(
            buttonSettingsUseCase.setSizePercent(
                uiState.value.settings,
                type,
                sizePercent,
                getApplication<Application>().resources.displayMetrics.density
            )
        )

    fun setBackgroundColor(type: NavButtonType, colorArgb: Long) =
        persistCurrentConfiguration(buttonSettingsUseCase.setBackgroundColor(uiState.value.settings, type, colorArgb))

    fun setBackgroundOpacity(type: NavButtonType, opacity: Float) =
        persistCurrentConfiguration(buttonSettingsUseCase.setBackgroundOpacity(uiState.value.settings, type, opacity))

    fun setBackgroundSize(type: NavButtonType, sizePercent: Int) =
        persistCurrentConfiguration(buttonSettingsUseCase.setBackgroundSizePercent(uiState.value.settings, type, sizePercent))

    fun setBackgroundSoftness(type: NavButtonType, softnessPercent: Int) =
        persistCurrentConfiguration(buttonSettingsUseCase.setBackgroundSoftnessPercent(uiState.value.settings, type, softnessPercent))

    fun setTheme(type: NavButtonType, themeId: String) =
        persistCurrentConfiguration(buttonSettingsUseCase.setTheme(uiState.value.settings, type, themeId))

    fun setEditMode(editMode: Boolean) {
        this.editMode.value = editMode
    }

    fun openEditMode() {
        setEditMode(true)
    }

    fun closeEditMode() {
        precisionDialogOpen.value = false
        positionBackgroundDialogOpen.value = false
        setEditMode(false)
    }

    fun setPrecisionStep(stepPx: Int) {
        val sanitized = buttonSettingsUseCase.setPrecisionStep(uiState.value.settings, stepPx).precisionStepPx
        viewModelScope.launch {
            settingsRepository.setPrecisionStep(sanitized)
        }
    }

    fun moveSelectedButton(deltaX: Float, deltaY: Float) {
        persistCurrentConfiguration(
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
        persistCurrentConfiguration(
            buttonSettingsUseCase.setPosition(
                uiState.value.settings,
                type,
                x,
                y
            )
        )
    }

    fun resetSelectedButtonPosition() {
        persistCurrentConfiguration(
            buttonSettingsUseCase.resetPosition(
                uiState.value.settings,
                uiState.value.settings.selectedButtonType,
                OverlayViewport.metrics(getApplication())
            )
        )
    }

    fun setButtonPositions(positions: Map<NavButtonType, Pair<Int, Int>>) {
        persistCurrentConfiguration(
            buttonSettingsUseCase.setPositions(
                uiState.value.settings,
                positions
            )
        )
    }

    fun setPrecisionDialogOpen(open: Boolean) {
        precisionDialogOpen.value = if (uiState.value.settings.editMode) open else false
    }

    fun setPositionBackground(uri: Uri) {
        viewModelScope.launch {
            positionBackgroundMutex.withLock {
                val resolver = getApplication<Application>().contentResolver
                val uriString = uri.toString()
                val targetPackage = uiState.value.selectedConfigurationPackage
                val previousUri = settingsRepository.positionBackgroundUriFlow(targetPackage).first()
                val alreadyPersisted = hasPersistedReadPermission(uri)
                var permissionAdded = false
                try {
                    if (!alreadyPersisted) {
                        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        permissionAdded = true
                    }
                    positionBackgroundImageLoader.validate(uri)
                    settingsRepository.setPositionBackgroundUri(uriString, targetPackage)
                    if (previousUri != null && previousUri != uriString) {
                        releaseUnusedReadPermission(previousUri)
                    }
                } catch (exception: Exception) {
                    if (exception is CancellationException) throw exception
                    if (permissionAdded && previousUri != uriString) {
                        releasePersistedReadPermission(uri)
                    }
                    Log.w(TAG, "Could not persist positioning background URI", exception)
                    message.value = getApplication<Application>().getString(R.string.error_position_background_load)
                }
            }
        }
    }

    fun setPositionBackgroundDialogOpen(open: Boolean) {
        positionBackgroundDialogOpen.value = if (uiState.value.settings.editMode) open else false
    }

    fun setPositionBackgroundAlpha(alpha: Int) {
        viewModelScope.launch {
            settingsRepository.setPositionBackgroundAlpha(
                alpha = alpha,
                packageName = uiState.value.selectedConfigurationPackage
            )
        }
    }

    fun setAppThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            settingsRepository.setAppThemeMode(mode)
        }
    }

    fun setBurnInProtectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBurnInProtectionEnabled(enabled)
        }
    }

    fun clearPositionBackground() {
        clearPositionBackground(expectedUri = null, showError = false)
    }

    fun handlePositionBackgroundLoadError(uri: String) {
        clearPositionBackground(expectedUri = uri, showError = true)
    }

    fun toggleApp(packageName: String, enabled: Boolean) {
        if (!enabled && selectedConfigurationPackage.value == packageName) {
            selectedConfigurationPackage.value = null
        }
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

    private fun clearPositionBackground(expectedUri: String?, showError: Boolean) {
        viewModelScope.launch {
            positionBackgroundMutex.withLock {
                val targetPackage = uiState.value.selectedConfigurationPackage
                val currentUri = settingsRepository.positionBackgroundUriFlow(targetPackage).first()
                if (expectedUri != null && currentUri != expectedUri) return@withLock
                settingsRepository.setPositionBackgroundUri(null, targetPackage)
                currentUri?.let { releaseUnusedReadPermission(it) }
                if (showError) {
                    message.value = getApplication<Application>().getString(R.string.error_position_background_load)
                }
            }
        }
    }

    /**
     * Releases the read permission of a background image only when no other configuration still
     * refers to it - the same image may be shared between the default layout and several apps.
     */
    private suspend fun releaseUnusedReadPermission(uriString: String) {
        if (settingsRepository.positionBackgroundUrisInUse().contains(uriString)) return
        releasePersistedReadPermission(Uri.parse(uriString))
    }

    private fun hasPersistedReadPermission(uri: Uri): Boolean = runCatching {
        getApplication<Application>().contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == uri && permission.isReadPermission
        }
    }.getOrDefault(false)

    private fun releasePersistedReadPermission(uri: Uri) {
        if (!hasPersistedReadPermission(uri)) return
        runCatching {
            getApplication<Application>().contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }.onFailure { exception ->
            Log.w(TAG, "Could not release positioning background URI", exception)
        }
    }

    private suspend fun loadInstalledApps(force: Boolean = false) {
        appsScanMutex.withLock {
            if (appsScanned && !force) return
            appsLoading.value = true
            try {
                installedApps.value = installedAppsRepository.loadApps()
                appsScanned = true
            } finally {
                appsLoading.value = false
            }
        }
    }

    private fun persistCurrentConfiguration(settings: OverlaySettings) {
        val state = uiState.value
        val targetPackageName = state.selectedConfigurationPackage
        viewModelScope.launch {
            if (targetPackageName == null) {
                settingsRepository.saveSettings(settings.copy(editMode = false))
            } else if (state.appConfigurations[targetPackageName]?.individualEnabled == true) {
                settingsRepository.saveAppConfiguration(targetPackageName, settings.buttons)
            }
        }
    }
}

private const val TAG = "MainViewModel"
