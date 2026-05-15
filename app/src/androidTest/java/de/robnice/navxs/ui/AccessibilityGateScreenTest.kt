package de.robnice.navxs.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.robnice.navxs.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityGateScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun gateScreenShowsRequiredActions() {
        composeRule.setContent {
            AccessibilityGateScreen(
                onOpenSettings = {},
                onCheckAgain = {}
            )
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.accessibility_required_title)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.open_accessibility_settings)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.check_again)).assertIsDisplayed()
    }
}
