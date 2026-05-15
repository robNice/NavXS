package de.robnice.navxs.data

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
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

    private fun createRepository(fileName: String): SettingsRepository {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(context.filesDir, fileName).apply { delete() } }
        )
        return SettingsRepository(dataStore)
    }
}
