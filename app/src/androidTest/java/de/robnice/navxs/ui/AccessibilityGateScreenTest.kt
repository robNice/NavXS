package de.robnice.navxs.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.robnice.navxs.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccessibilityGateScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int) = composeRule.activity.getString(id)

    @Test
    fun gateScreenShowsTwoExplicitConsentButtons() {
        composeRule.setContent {
            AccessibilityGateScreen(onAgree = {}, onDecline = {})
        }

        composeRule.onNodeWithText(text(R.string.disclosure_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.disclosure_agree)).assertExists().assertIsEnabled()
        composeRule.onNodeWithText(text(R.string.disclosure_decline)).assertExists().assertIsEnabled()
    }

    @Test
    fun agreeAndDeclineInvokeSeparateCallbacks() {
        var agreed = 0
        var declined = 0
        composeRule.setContent {
            AccessibilityGateScreen(onAgree = { agreed++ }, onDecline = { declined++ })
        }

        composeRule.onNodeWithText(text(R.string.disclosure_decline)).assertIsDisplayed().performClick()
        assertEquals(0, agreed)
        assertEquals(1, declined)

        composeRule.onNodeWithText(text(R.string.disclosure_agree)).assertIsDisplayed().performClick()
        assertEquals(1, agreed)
        assertEquals(1, declined)
    }
}
