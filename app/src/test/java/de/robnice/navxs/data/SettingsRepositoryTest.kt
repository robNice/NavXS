package de.robnice.navxs.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import de.robnice.navxs.data.models.AppThemeMode
import de.robnice.navxs.data.models.NavButtonType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {
    @Test
    fun settingsAreSavedAndLoaded() = runTest {
        val repository = createRepository("settings_saved.preferences_pb")
        val settings = NavDefaults.defaultOverlaySettings().copy(
            selectedButtonType = NavButtonType.HOME,
            editMode = true,
            precisionStepPx = 10,
            buttons = NavDefaults.defaultOverlaySettings().buttons.toMutableMap().apply {
                this[NavButtonType.HOME] = getValue(NavButtonType.HOME).copy(sizePercent = 180)
            }
        )

        repository.saveSettings(settings)

        val loaded = repository.settingsFlow.first()
        assertThat(loaded.selectedButtonType).isEqualTo(NavButtonType.HOME)
        assertThat(loaded.editMode).isFalse()
        assertThat(loaded.precisionStepPx).isEqualTo(10)
        assertThat(loaded.buttons.getValue(NavButtonType.HOME).sizePercent).isEqualTo(180)
    }

    @Test
    fun appSelectionAndFlagsAreSavedAndLoaded() = runTest {
        val repository = createRepository("apps_saved.preferences_pb")

        repository.saveSelectedApps(setOf("a.package", "b.package"))
        repository.setShowSystemApps(true)
        repository.setSelectedTab(1)

        assertThat(repository.selectedAppsFlow.first()).containsExactly("a.package", "b.package")
        assertThat(repository.showSystemAppsFlow.first()).isTrue()
        assertThat(repository.selectedTabFlow.first()).isEqualTo(1)
    }

    @Test
    fun thirdTabIsPersistedAndInvalidTabFallsBackToApps() = runTest {
        val repository = createRepository("third_tab_saved.preferences_pb")

        repository.setSelectedTab(2)
        assertThat(repository.selectedTabFlow.first()).isEqualTo(2)

        repository.setSelectedTab(99)
        assertThat(repository.selectedTabFlow.first()).isEqualTo(0)
    }

    @Test
    fun globalAppearanceAndBurnInProtectionAreSavedAndLoaded() = runTest {
        val repository = createRepository("general_settings_saved.preferences_pb")

        assertThat(repository.appThemeModeFlow.first()).isEqualTo(AppThemeMode.SYSTEM)
        assertThat(repository.burnInProtectionEnabledFlow.first()).isFalse()

        repository.setAppThemeMode(AppThemeMode.DARK)
        repository.setBurnInProtectionEnabled(true)

        assertThat(repository.appThemeModeFlow.first()).isEqualTo(AppThemeMode.DARK)
        assertThat(repository.burnInProtectionEnabledFlow.first()).isTrue()
    }

    @Test
    fun enablingIndividualConfigurationCopiesCurrentDefaultLayout() = runTest {
        val repository = createRepository("individual_config_enabled.preferences_pb")
        val defaults = NavDefaults.defaultOverlaySettings().copy(
            buttons = NavDefaults.defaultOverlaySettings().buttons.toMutableMap().apply {
                this[NavButtonType.BACK] = getValue(NavButtonType.BACK).copy(colorArgb = 0xFF123456)
            }
        )
        repository.saveSettings(defaults)

        repository.setIndividualAppConfigurationEnabled("example.app", true)

        val configuration = repository.appConfigurationsFlow.first().getValue("example.app")
        assertThat(configuration.individualEnabled).isTrue()
        assertThat(configuration.buttons).isEqualTo(defaults.buttons)
    }

    @Test
    fun disablingIndividualConfigurationPreservesItsValues() = runTest {
        val repository = createRepository("individual_config_preserved.preferences_pb")
        repository.setIndividualAppConfigurationEnabled("example.app", true)
        val customButtons = NavDefaults.defaultOverlaySettings().buttons.toMutableMap().apply {
            this[NavButtonType.HOME] = getValue(NavButtonType.HOME).copy(
                sizePercent = 180,
                positionXPx = 321
            )
        }
        repository.saveAppConfiguration("example.app", customButtons)

        repository.setIndividualAppConfigurationEnabled("example.app", false)

        val disabled = repository.appConfigurationsFlow.first().getValue("example.app")
        assertThat(disabled.individualEnabled).isFalse()
        assertThat(disabled.buttons.getValue(NavButtonType.HOME).sizePercent).isEqualTo(180)
        assertThat(disabled.buttons.getValue(NavButtonType.HOME).positionXPx).isEqualTo(321)

        repository.setIndividualAppConfigurationEnabled("example.app", true)
        val enabledAgain = repository.appConfigurationsFlow.first().getValue("example.app")
        assertThat(enabledAgain.buttons).isEqualTo(disabled.buttons)
    }

    @Test
    fun positionBackgroundUriIsSavedAndLoaded() = runTest {
        val repository = createRepository("position_background_saved.preferences_pb")
        val uri = "content://media/picker/0/com.android.providers.media.photopicker/media/42"

        repository.setPositionBackgroundUri(uri)

        assertThat(repository.positionBackgroundUriFlow.first()).isEqualTo(uri)
    }

    @Test
    fun positionBackgroundUriCanBeCleared() = runTest {
        val repository = createRepository("position_background_cleared.preferences_pb")
        repository.setPositionBackgroundUri("content://screenshots/example")

        repository.setPositionBackgroundUri(null)

        assertThat(repository.positionBackgroundUriFlow.first()).isNull()
    }

    @Test
    fun blankPositionBackgroundUriIsTreatedAsMissing() = runTest {
        val repository = createRepository("position_background_blank.preferences_pb")

        repository.setPositionBackgroundUri("   ")

        assertThat(repository.positionBackgroundUriFlow.first()).isNull()
    }

    @Test
    fun positionBackgroundAlphaDefaultsToFullyOpaque() = runTest {
        val repository = createRepository("position_background_alpha_default.preferences_pb")

        assertThat(repository.positionBackgroundAlphaFlow.first())
            .isEqualTo(SettingsRepository.DefaultPositionBackgroundAlpha)
    }

    @Test
    fun positionBackgroundAlphaIsSavedAndCoerced() = runTest {
        val repository = createRepository("position_background_alpha_saved.preferences_pb")

        repository.setPositionBackgroundAlpha(45)
        assertThat(repository.positionBackgroundAlphaFlow.first()).isEqualTo(45)

        repository.setPositionBackgroundAlpha(0)
        assertThat(repository.positionBackgroundAlphaFlow.first())
            .isEqualTo(SettingsRepository.MinPositionBackgroundAlpha)

        repository.setPositionBackgroundAlpha(500)
        assertThat(repository.positionBackgroundAlphaFlow.first())
            .isEqualTo(SettingsRepository.MaxPositionBackgroundAlpha)
    }

    @Test
    fun clearingPositionBackgroundUriKeepsAlpha() = runTest {
        val repository = createRepository("position_background_alpha_kept.preferences_pb")
        repository.setPositionBackgroundAlpha(30)
        repository.setPositionBackgroundUri("content://screenshots/example")

        repository.setPositionBackgroundUri(null)

        assertThat(repository.positionBackgroundAlphaFlow.first()).isEqualTo(30)
    }

    @Test
    fun positionBackgroundIsStoredPerConfiguration() = runTest {
        val repository = createRepository("position_background_per_config.preferences_pb")

        repository.setPositionBackgroundUri("content://screenshots/default")
        repository.setPositionBackgroundAlpha(40)
        repository.setPositionBackgroundUri("content://screenshots/app", packageName = "example.app")
        repository.setPositionBackgroundAlpha(90, packageName = "example.app")

        assertThat(repository.positionBackgroundUriFlow(null).first())
            .isEqualTo("content://screenshots/default")
        assertThat(repository.positionBackgroundAlphaFlow(null).first()).isEqualTo(40)
        assertThat(repository.positionBackgroundUriFlow("example.app").first())
            .isEqualTo("content://screenshots/app")
        assertThat(repository.positionBackgroundAlphaFlow("example.app").first()).isEqualTo(90)
    }

    @Test
    fun clearingOneConfigurationKeepsTheBackgroundOfTheOthers() = runTest {
        val repository = createRepository("position_background_independent.preferences_pb")
        repository.setPositionBackgroundUri("content://screenshots/default")
        repository.setPositionBackgroundUri("content://screenshots/app", packageName = "example.app")

        repository.setPositionBackgroundUri(null, packageName = "example.app")

        assertThat(repository.positionBackgroundUriFlow("example.app").first()).isNull()
        assertThat(repository.positionBackgroundUriFlow(null).first())
            .isEqualTo("content://screenshots/default")
    }

    @Test
    fun backgroundUrisInUseCoverEveryConfiguration() = runTest {
        val repository = createRepository("position_background_in_use.preferences_pb")
        repository.setPositionBackgroundUri("content://screenshots/default")
        repository.setPositionBackgroundUri("content://screenshots/app", packageName = "example.app")

        assertThat(repository.positionBackgroundUrisInUse())
            .containsExactly("content://screenshots/default", "content://screenshots/app")

        repository.setPositionBackgroundUri(null, packageName = "example.app")

        assertThat(repository.positionBackgroundUrisInUse())
            .containsExactly("content://screenshots/default")
    }

    private fun createRepository(fileName: String): SettingsRepository {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(context.filesDir, fileName).apply { delete() } }
        )
        return SettingsRepository(dataStore, context.resources.displayMetrics)
    }
}
