package de.robnice.navxs.data

import android.content.Context
import android.util.DisplayMetrics
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import de.robnice.navxs.data.models.AppOverlayConfiguration
import de.robnice.navxs.data.models.AppThemeMode
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
import de.robnice.navxs.data.models.OverlaySettings
import de.robnice.navxs.overlay.OverlayViewport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val displayMetrics: DisplayMetrics
) {
    private val preferencesFlow: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }

    val settingsFlow: Flow<OverlaySettings> = preferencesFlow.map(::preferencesToSettings)

    val appConfigurationsFlow: Flow<Map<String, AppOverlayConfiguration>> =
        preferencesFlow.map(::preferencesToAppConfigurations)

    val appThemeModeFlow: Flow<AppThemeMode> = preferencesFlow.map { preferences ->
        preferences[Keys.AppThemeMode]
            ?.let { stored -> runCatching { AppThemeMode.valueOf(stored) }.getOrNull() }
            ?: AppThemeMode.SYSTEM
    }

    val burnInProtectionEnabledFlow: Flow<Boolean> = preferencesFlow.map { preferences ->
        preferences[Keys.BurnInProtectionEnabled] ?: false
    }

    val selectedAppsFlow: Flow<Set<String>> = preferencesFlow
        .map { preferences ->
            preferences[Keys.SelectedApps]?.split("|")?.filter { it.isNotBlank() }?.toSet().orEmpty()
        }

    val showSystemAppsFlow: Flow<Boolean> = preferencesFlow
        .map { it[Keys.ShowSystemApps] ?: false }

    val selectedTabFlow: Flow<Int> = preferencesFlow.map { preferences ->
        preferences[Keys.SelectedTab]?.takeIf { it in 0..2 } ?: 0
    }

    val positionBackgroundUriFlow: Flow<String?> = preferencesFlow
        .map { preferences -> readPositionBackgroundUri(preferences, packageName = null) }

    /**
     * Positioning background of one configuration. [packageName] null addresses the default layout,
     * which keeps the original keys so existing installations do not lose their background.
     */
    fun positionBackgroundUriFlow(packageName: String?): Flow<String?> = preferencesFlow
        .map { preferences -> readPositionBackgroundUri(preferences, packageName) }

    suspend fun saveSettings(settings: OverlaySettings) {
        dataStore.edit { preferences ->
            preferences[Keys.SelectedButtonType] = settings.selectedButtonType.name
            preferences[Keys.PrecisionStepPx] = sanitizePrecisionStep(settings.precisionStepPx)
            writeDefaultButtons(preferences, settings.buttons)
        }
    }

    suspend fun saveAppConfiguration(
        packageName: String,
        buttons: Map<NavButtonType, OverlayButtonConfig>
    ) {
        if (packageName.isBlank()) return
        dataStore.edit { preferences ->
            val fallback = preferencesToSettings(preferences).buttons
            val completeButtons = NavButtonType.entries.associateWith { type ->
                buttons[type]?.copy(type = type) ?: fallback.getValue(type)
            }
            preferences[Keys.ConfiguredAppPackages] =
                (configuredAppPackages(preferences) + packageName).sorted().joinToString("|")
            writeAppButtons(preferences, packageName, completeButtons)
        }
    }

    suspend fun setIndividualAppConfigurationEnabled(packageName: String, enabled: Boolean) {
        if (packageName.isBlank()) return
        dataStore.edit { preferences ->
            val configuredPackages = configuredAppPackages(preferences)
            if (packageName !in configuredPackages) {
                writeAppButtons(
                    preferences = preferences,
                    packageName = packageName,
                    buttons = preferencesToSettings(preferences).buttons
                )
            }
            preferences[Keys.ConfiguredAppPackages] =
                (configuredPackages + packageName).sorted().joinToString("|")
            preferences[Keys.appIndividualEnabledKey(packageName)] = enabled
        }
    }

    suspend fun setSelectedButtonType(type: NavButtonType) {
        dataStore.edit { preferences ->
            preferences[Keys.SelectedButtonType] = type.name
        }
    }

    suspend fun setPrecisionStep(stepPx: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.PrecisionStepPx] = sanitizePrecisionStep(stepPx)
        }
    }

    suspend fun setAppThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.AppThemeMode] = mode.name
        }
    }

    suspend fun setBurnInProtectionEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.BurnInProtectionEnabled] = enabled
        }
    }

    suspend fun saveSelectedApps(packageNames: Set<String>) {
        dataStore.edit { preferences ->
            preferences[Keys.SelectedApps] = packageNames.sorted().joinToString("|")
        }
    }

    suspend fun setShowSystemApps(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ShowSystemApps] = show
        }
    }

    suspend fun setSelectedTab(index: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.SelectedTab] = index.takeIf { it in 0..2 } ?: 0
        }
    }

    val positionBackgroundAlphaFlow: Flow<Int> = preferencesFlow
        .map { preferences -> readPositionBackgroundAlpha(preferences, packageName = null) }

    fun positionBackgroundAlphaFlow(packageName: String?): Flow<Int> = preferencesFlow
        .map { preferences -> readPositionBackgroundAlpha(preferences, packageName) }

    suspend fun setPositionBackgroundAlpha(alpha: Int, packageName: String? = null) {
        dataStore.edit { preferences ->
            preferences[Keys.positionBackgroundAlphaKey(packageName)] =
                sanitizePositionBackgroundAlpha(alpha)
        }
    }

    suspend fun setPositionBackgroundUri(uri: String?, packageName: String? = null) {
        dataStore.edit { preferences ->
            val key = Keys.positionBackgroundUriKey(packageName)
            if (uri.isNullOrBlank()) {
                preferences.remove(key)
            } else {
                preferences[key] = uri
            }
        }
    }

    /** Every background image that is still referenced by any configuration. */
    suspend fun positionBackgroundUrisInUse(): Set<String> {
        val preferences = preferencesFlow.first()
        return preferences.asMap()
            .filterKeys { key -> key.name.endsWith(PositionBackgroundUriSuffix) }
            .values
            .filterIsInstance<String>()
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun readPositionBackgroundUri(preferences: Preferences, packageName: String?): String? =
        preferences[Keys.positionBackgroundUriKey(packageName)]?.takeIf { it.isNotBlank() }

    private fun readPositionBackgroundAlpha(preferences: Preferences, packageName: String?): Int =
        sanitizePositionBackgroundAlpha(preferences[Keys.positionBackgroundAlphaKey(packageName)])

    private fun preferencesToSettings(preferences: Preferences): OverlaySettings {
        val defaults = NavDefaults.defaultOverlaySettings(displayMetrics)
        val selectedType = preferences[Keys.SelectedButtonType]
            ?.let { runCatching { NavButtonType.valueOf(it) }.getOrNull() }
            ?: defaults.selectedButtonType
        val parsedButtons = NavButtonType.entries.associateWith { type ->
            val fallback = defaults.buttons.getValue(type)
            OverlayButtonConfig(
                type = type,
                active = preferences[Keys.activeKey(type)] ?: fallback.active,
                colorArgb = preferences[Keys.colorKey(type)] ?: fallback.colorArgb,
                opacity = (preferences[Keys.opacityKey(type)] ?: fallback.opacity).coerceIn(0f, 1f),
                sizePercent = (preferences[Keys.sizeKey(type)] ?: fallback.sizePercent).coerceIn(100, 300),
                backgroundColorArgb = preferences[Keys.backgroundColorKey(type)] ?: fallback.backgroundColorArgb,
                backgroundOpacity = (preferences[Keys.backgroundOpacityKey(type)] ?: fallback.backgroundOpacity).coerceIn(0f, 1f),
                backgroundSizePercent = (preferences[Keys.backgroundSizeKey(type)] ?: fallback.backgroundSizePercent).coerceIn(25, 300),
                backgroundSoftnessPercent = (preferences[Keys.backgroundSoftnessKey(type)] ?: fallback.backgroundSoftnessPercent).coerceIn(0, 100),
                positionXPx = preferences[Keys.xKey(type)] ?: fallback.positionXPx,
                positionYPx = preferences[Keys.yKey(type)] ?: fallback.positionYPx,
                themeId = preferences[Keys.themeKey(type)] ?: fallback.themeId
            )
        }
        val buttons = parsedButtons.takeIf { values -> values.values.any { it.active } }
            ?: defaults.buttons
        return defaults.copy(
            selectedButtonType = selectedType,
            editMode = false,
            precisionStepPx = sanitizePrecisionStep(preferences[Keys.PrecisionStepPx] ?: defaults.precisionStepPx),
            buttons = buttons
        )
    }

    private fun preferencesToAppConfigurations(
        preferences: Preferences
    ): Map<String, AppOverlayConfiguration> {
        val defaultButtons = preferencesToSettings(preferences).buttons
        return configuredAppPackages(preferences).associateWith { packageName ->
            val parsedButtons = NavButtonType.entries.associateWith { type ->
                val fallback = defaultButtons.getValue(type)
                OverlayButtonConfig(
                    type = type,
                    active = preferences[Keys.appActiveKey(packageName, type)] ?: fallback.active,
                    colorArgb = preferences[Keys.appColorKey(packageName, type)] ?: fallback.colorArgb,
                    opacity = (preferences[Keys.appOpacityKey(packageName, type)] ?: fallback.opacity)
                        .coerceIn(0f, 1f),
                    sizePercent = (preferences[Keys.appSizeKey(packageName, type)] ?: fallback.sizePercent)
                        .coerceIn(100, 300),
                    backgroundColorArgb =
                        preferences[Keys.appBackgroundColorKey(packageName, type)] ?: fallback.backgroundColorArgb,
                    backgroundOpacity =
                        (preferences[Keys.appBackgroundOpacityKey(packageName, type)] ?: fallback.backgroundOpacity)
                            .coerceIn(0f, 1f),
                    backgroundSizePercent =
                        (preferences[Keys.appBackgroundSizeKey(packageName, type)] ?: fallback.backgroundSizePercent)
                            .coerceIn(25, 300),
                    backgroundSoftnessPercent =
                        (preferences[Keys.appBackgroundSoftnessKey(packageName, type)]
                            ?: fallback.backgroundSoftnessPercent).coerceIn(0, 100),
                    positionXPx = preferences[Keys.appXKey(packageName, type)] ?: fallback.positionXPx,
                    positionYPx = preferences[Keys.appYKey(packageName, type)] ?: fallback.positionYPx,
                    themeId = preferences[Keys.appThemeKey(packageName, type)] ?: fallback.themeId
                )
            }
            AppOverlayConfiguration(
                individualEnabled = preferences[Keys.appIndividualEnabledKey(packageName)] ?: false,
                buttons = parsedButtons.takeIf { values -> values.values.any { it.active } } ?: defaultButtons
            )
        }
    }

    private fun configuredAppPackages(preferences: Preferences): Set<String> =
        preferences[Keys.ConfiguredAppPackages]
            ?.split("|")
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()

    private fun writeDefaultButtons(
        preferences: MutablePreferences,
        buttons: Map<NavButtonType, OverlayButtonConfig>
    ) {
        buttons.values.forEach { button ->
            preferences[Keys.activeKey(button.type)] = button.active
            preferences[Keys.colorKey(button.type)] = button.colorArgb
            preferences[Keys.opacityKey(button.type)] = button.opacity.coerceIn(0f, 1f)
            preferences[Keys.sizeKey(button.type)] = button.sizePercent.coerceIn(25, 300)
            preferences[Keys.backgroundColorKey(button.type)] = button.backgroundColorArgb
            preferences[Keys.backgroundOpacityKey(button.type)] = button.backgroundOpacity.coerceIn(0f, 1f)
            preferences[Keys.backgroundSizeKey(button.type)] = button.backgroundSizePercent.coerceIn(25, 300)
            preferences[Keys.backgroundSoftnessKey(button.type)] =
                button.backgroundSoftnessPercent.coerceIn(0, 100)
            preferences[Keys.xKey(button.type)] = button.positionXPx
            preferences[Keys.yKey(button.type)] = button.positionYPx
            preferences[Keys.themeKey(button.type)] = button.themeId
        }
    }

    private fun writeAppButtons(
        preferences: MutablePreferences,
        packageName: String,
        buttons: Map<NavButtonType, OverlayButtonConfig>
    ) {
        buttons.values.forEach { button ->
            preferences[Keys.appActiveKey(packageName, button.type)] = button.active
            preferences[Keys.appColorKey(packageName, button.type)] = button.colorArgb
            preferences[Keys.appOpacityKey(packageName, button.type)] = button.opacity.coerceIn(0f, 1f)
            preferences[Keys.appSizeKey(packageName, button.type)] = button.sizePercent.coerceIn(25, 300)
            preferences[Keys.appBackgroundColorKey(packageName, button.type)] = button.backgroundColorArgb
            preferences[Keys.appBackgroundOpacityKey(packageName, button.type)] =
                button.backgroundOpacity.coerceIn(0f, 1f)
            preferences[Keys.appBackgroundSizeKey(packageName, button.type)] =
                button.backgroundSizePercent.coerceIn(25, 300)
            preferences[Keys.appBackgroundSoftnessKey(packageName, button.type)] =
                button.backgroundSoftnessPercent.coerceIn(0, 100)
            preferences[Keys.appXKey(packageName, button.type)] = button.positionXPx
            preferences[Keys.appYKey(packageName, button.type)] = button.positionYPx
            preferences[Keys.appThemeKey(packageName, button.type)] = button.themeId
        }
    }

    private fun sanitizePrecisionStep(step: Int): Int = when (step) {
        1, 5, 10 -> step
        else -> NavDefaults.DefaultPrecisionStepPx
    }

    private fun sanitizePositionBackgroundAlpha(alpha: Int?): Int =
        (alpha ?: DefaultPositionBackgroundAlpha).coerceIn(
            MinPositionBackgroundAlpha,
            MaxPositionBackgroundAlpha
        )

    object Keys {
        val SelectedApps = stringPreferencesKey("selected_apps")
        val ShowSystemApps = booleanPreferencesKey("show_system_apps")
        val SelectedTab = intPreferencesKey("selected_tab")
        val SelectedButtonType = stringPreferencesKey("selected_button_type")
        val PrecisionStepPx = intPreferencesKey("precision_step_px")
        val PositionBackgroundUri = stringPreferencesKey("position_background_uri")
        val PositionBackgroundAlpha = intPreferencesKey("position_background_alpha")
        val ConfiguredAppPackages = stringPreferencesKey("configured_app_packages")
        val AppThemeMode = stringPreferencesKey("app_theme_mode")
        val BurnInProtectionEnabled = booleanPreferencesKey("burn_in_protection_enabled")

        fun activeKey(type: NavButtonType) = booleanPreferencesKey("${type.name.lowercase()}_active")
        fun colorKey(type: NavButtonType) = longPreferencesKey("${type.name.lowercase()}_color")
        fun opacityKey(type: NavButtonType) = floatPreferencesKey("${type.name.lowercase()}_opacity")
        fun sizeKey(type: NavButtonType) = intPreferencesKey("${type.name.lowercase()}_size")
        fun backgroundColorKey(type: NavButtonType) = longPreferencesKey("${type.name.lowercase()}_background_color")
        fun backgroundOpacityKey(type: NavButtonType) = floatPreferencesKey("${type.name.lowercase()}_background_opacity")
        fun backgroundSizeKey(type: NavButtonType) = intPreferencesKey("${type.name.lowercase()}_background_size")
        fun backgroundSoftnessKey(type: NavButtonType) = intPreferencesKey("${type.name.lowercase()}_background_softness")
        fun xKey(type: NavButtonType) = intPreferencesKey("${type.name.lowercase()}_x")
        fun yKey(type: NavButtonType) = intPreferencesKey("${type.name.lowercase()}_y")
        fun themeKey(type: NavButtonType) = stringPreferencesKey("${type.name.lowercase()}_theme")

        fun positionBackgroundUriKey(packageName: String?) = packageName
            ?.let { stringPreferencesKey("app_config_${it}_position_background_uri") }
            ?: PositionBackgroundUri

        fun positionBackgroundAlphaKey(packageName: String?) = packageName
            ?.let { intPreferencesKey("app_config_${it}_position_background_alpha") }
            ?: PositionBackgroundAlpha

        fun appIndividualEnabledKey(packageName: String) =
            booleanPreferencesKey("app_config_${packageName}_individual_enabled")
        fun appActiveKey(packageName: String, type: NavButtonType) =
            booleanPreferencesKey(appButtonKey(packageName, type, "active"))
        fun appColorKey(packageName: String, type: NavButtonType) =
            longPreferencesKey(appButtonKey(packageName, type, "color"))
        fun appOpacityKey(packageName: String, type: NavButtonType) =
            floatPreferencesKey(appButtonKey(packageName, type, "opacity"))
        fun appSizeKey(packageName: String, type: NavButtonType) =
            intPreferencesKey(appButtonKey(packageName, type, "size"))
        fun appBackgroundColorKey(packageName: String, type: NavButtonType) =
            longPreferencesKey(appButtonKey(packageName, type, "background_color"))
        fun appBackgroundOpacityKey(packageName: String, type: NavButtonType) =
            floatPreferencesKey(appButtonKey(packageName, type, "background_opacity"))
        fun appBackgroundSizeKey(packageName: String, type: NavButtonType) =
            intPreferencesKey(appButtonKey(packageName, type, "background_size"))
        fun appBackgroundSoftnessKey(packageName: String, type: NavButtonType) =
            intPreferencesKey(appButtonKey(packageName, type, "background_softness"))
        fun appXKey(packageName: String, type: NavButtonType) =
            intPreferencesKey(appButtonKey(packageName, type, "x"))
        fun appYKey(packageName: String, type: NavButtonType) =
            intPreferencesKey(appButtonKey(packageName, type, "y"))
        fun appThemeKey(packageName: String, type: NavButtonType) =
            stringPreferencesKey(appButtonKey(packageName, type, "theme"))

        private fun appButtonKey(packageName: String, type: NavButtonType, value: String) =
            "app_config_${packageName}_${type.name.lowercase()}_$value"
    }

    companion object {
        private const val PositionBackgroundUriSuffix = "position_background_uri"
        const val DefaultPositionBackgroundAlpha = 100
        const val MinPositionBackgroundAlpha = 10
        const val MaxPositionBackgroundAlpha = 100

        @Volatile
        private var instance: SettingsRepository? = null

        fun create(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.applicationContext.filesDir, "navxs_settings.preferences_pb") }
                    ),
                    OverlayViewport.metrics(context.applicationContext)
                ).also { instance = it }
            }
        }
    }
}
