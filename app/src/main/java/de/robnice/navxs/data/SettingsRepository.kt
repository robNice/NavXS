package de.robnice.navxs.data

import android.content.Context
import android.util.DisplayMetrics
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import de.robnice.navxs.data.models.NavButtonType
import de.robnice.navxs.data.models.OverlayButtonConfig
import de.robnice.navxs.data.models.OverlaySettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val displayMetrics: DisplayMetrics
) {
    val settingsFlow: Flow<OverlaySettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map(::preferencesToSettings)

    val selectedAppsFlow: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[Keys.SelectedApps]?.split("|")?.filter { it.isNotBlank() }?.toSet().orEmpty()
        }

    val showSystemAppsFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[Keys.ShowSystemApps] ?: false }

    val selectedTabFlow: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { (it[Keys.SelectedTab] ?: 0).coerceIn(0, 1) }

    suspend fun saveSettings(settings: OverlaySettings) {
        dataStore.edit { preferences ->
            preferences[Keys.SelectedButtonType] = settings.selectedButtonType.name
            preferences[Keys.PrecisionStepPx] = sanitizePrecisionStep(settings.precisionStepPx)
            settings.buttons.values.forEach { button ->
                preferences[Keys.activeKey(button.type)] = button.active
                preferences[Keys.colorKey(button.type)] = button.colorArgb
                preferences[Keys.opacityKey(button.type)] = button.opacity.coerceIn(0f, 1f)
                preferences[Keys.sizeKey(button.type)] = button.sizePercent.coerceIn(25, 300)
                preferences[Keys.backgroundColorKey(button.type)] = button.backgroundColorArgb
                preferences[Keys.backgroundOpacityKey(button.type)] = button.backgroundOpacity.coerceIn(0f, 1f)
                preferences[Keys.backgroundSizeKey(button.type)] = button.backgroundSizePercent.coerceIn(25, 300)
                preferences[Keys.backgroundSoftnessKey(button.type)] = button.backgroundSoftnessPercent.coerceIn(0, 100)
                preferences[Keys.xKey(button.type)] = button.positionXPx
                preferences[Keys.yKey(button.type)] = button.positionYPx
                preferences[Keys.themeKey(button.type)] = button.themeId
            }
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
            preferences[Keys.SelectedTab] = index.coerceIn(0, 1)
        }
    }

    private fun preferencesToSettings(preferences: Preferences): OverlaySettings {
        val defaults = NavDefaults.defaultOverlaySettings(displayMetrics)
        val selectedType = preferences[Keys.SelectedButtonType]
            ?.let { runCatching { NavButtonType.valueOf(it) }.getOrNull() }
            ?: defaults.selectedButtonType
        val buttons = NavButtonType.entries.associateWith { type ->
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
        return defaults.copy(
            selectedButtonType = selectedType,
            editMode = false,
            precisionStepPx = sanitizePrecisionStep(preferences[Keys.PrecisionStepPx] ?: defaults.precisionStepPx),
            buttons = buttons
        )
    }

    private fun sanitizePrecisionStep(step: Int): Int = when (step) {
        1, 5, 10 -> step
        else -> NavDefaults.DefaultPrecisionStepPx
    }

    object Keys {
        val SelectedApps = stringPreferencesKey("selected_apps")
        val ShowSystemApps = booleanPreferencesKey("show_system_apps")
        val SelectedTab = intPreferencesKey("selected_tab")
        val SelectedButtonType = stringPreferencesKey("selected_button_type")
        val PrecisionStepPx = intPreferencesKey("precision_step_px")

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
    }

    companion object {
        @Volatile
        private var instance: SettingsRepository? = null

        fun create(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(
                    PreferenceDataStoreFactory.create(
                        produceFile = { File(context.applicationContext.filesDir, "navxs_settings.preferences_pb") }
                    ),
                    context.applicationContext.resources.displayMetrics
                ).also { instance = it }
            }
        }
    }
}
