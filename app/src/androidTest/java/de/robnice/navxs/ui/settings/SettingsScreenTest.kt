package de.robnice.navxs.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.robnice.navxs.R
import de.robnice.navxs.data.NavDefaults
import de.robnice.navxs.ui.MainUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun settingsScreenShowsSelectorAndControls() {
        composeRule.setContent {
            SettingsScreen(
                state = MainUiState(settings = NavDefaults.defaultOverlaySettings()),
                onSelectButton = {},
                onActiveChange = { _, _ -> },
                onColorChange = { _, _ -> },
                onOpacityChange = { _, _ -> },
                onSizeChange = { _, _ -> },
                onBackgroundColorChange = { _, _ -> },
                onBackgroundOpacityChange = { _, _ -> },
                onBackgroundSizeChange = { _, _ -> },
                onBackgroundSoftnessChange = { _, _ -> },
                onThemeChange = { _, _ -> },
                onOpenEditMode = {},
                onSelectConfiguration = {},
                onIndividualConfigurationChange = {},
                onCopyConfiguration = {}
            )
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_back)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.button_home)).assertIsDisplayed()
        composeRule
            .onAllNodesWithText(composeRule.activity.getString(R.string.settings_colour))
            .onFirst()
            .assertIsDisplayed()
    }
}
