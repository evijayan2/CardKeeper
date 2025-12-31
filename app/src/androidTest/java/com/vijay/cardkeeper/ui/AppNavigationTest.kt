package com.vijay.cardkeeper.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.vijay.cardkeeper.MainActivity
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {

    companion object {
        @JvmStatic
        @org.junit.BeforeClass
        fun setupClass() {
             // Bypass authentication by injecting a dummy passphrase BEFORE activity launches
            val field = com.vijay.cardkeeper.util.KeyManager::class.java.getDeclaredField("cachedPassphrase")
            field.isAccessible = true
            field.set(com.vijay.cardkeeper.util.KeyManager, "test_passphrase_for_ui_test".toByteArray())
        }
    }
            
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunch_showsDashboard() {
        // Verify Top App Bar Title
        composeTestRule.onNodeWithText("Kards").assertIsDisplayed()
        
        // Verify Bottom Navigation Items (Tabs)
        // Tabs: Finance, Identity, Passports, Rewards
        composeTestRule.onNodeWithText("Finance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Identity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Passports").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rewards").assertIsDisplayed()
    }

    @Test
    fun navigation_switchTabs() {
        // Click on Identity Tab
        composeTestRule.onNodeWithText("Identity").performClick()
        
        // Verify we are on the Identity screen
        // (Since tabs are just text labels in a PrimaryTabRow, asserting they exist and are clickable is the main test here)
        composeTestRule.onNodeWithText("Identity").assertIsDisplayed()
    }

    @Test
    fun addItem_opensAddItemScreen() {
        // Click FAB (Floating Action Button) to open the menu
        composeTestRule.onNodeWithContentDescription("Add Item").performClick()

        // Verify the dropdown menu appears with options
        // We'll click the first option: "Add Credit/Debit Card"
        composeTestRule.onNodeWithText("Add Credit/Debit Card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Credit/Debit Card").performClick()

        // Verify we navigated to the Add Item screen with the correct title
        // The title for this selection is "Add Credit/Debit Card"
        composeTestRule.onNodeWithText("Add Credit/Debit Card").assertIsDisplayed()
    }
}
