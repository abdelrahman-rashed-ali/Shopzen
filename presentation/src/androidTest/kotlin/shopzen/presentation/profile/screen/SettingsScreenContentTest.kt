package shopzen.presentation.profile.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.AppTheme
import shopzen.domain.profile.model.UserPreferences
import shopzen.presentation.profile.intent.SettingsIntent
import shopzen.presentation.profile.state.SettingsState
import shopzen.presentation.theme.ShopzenTheme

class SettingsScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun guestSettingsShowsShellPreferencesToolsAndLoginAction() {
        var loginClicks = 0

        composeTestRule.setContent {
            ShopzenTheme {
                SettingsContent(
                    state = SettingsState(isGuest = true),
                    onIntent = {},
                    onNavigateToDiscover = {},
                    onNavigateToSearch = {},
                    onNavigateToWishlist = {},
                    onNavigateToProfile = {},
                    onNavigateToCart = {},
                    onNavigateToLogin = { loginClicks++ },
                    onNavigateToAddresses = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag(SettingsTestTags.Preferences).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SettingsTestTags.AccountActions).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(SettingsTestTags.Tools).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved Addresses").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Cart").assertIsDisplayed()
        composeTestRule.onNodeWithTag(SettingsTestTags.LoginButton).performScrollTo().performClick()

        assertEquals(1, loginClicks)
    }

    @Test
    fun preferenceAndSyncActionsEmitSettingsIntents() {
        val intents = mutableListOf<SettingsIntent>()

        composeTestRule.setContent {
            ShopzenTheme {
                SettingsContent(
                    state = SettingsState(
                        isGuest = true,
                        preferences = UserPreferences(
                            currency = AppCurrency.USD,
                            theme = AppTheme.SYSTEM,
                        ),
                    ),
                    onIntent = { intents += it },
                    onNavigateToDiscover = {},
                    onNavigateToSearch = {},
                    onNavigateToWishlist = {},
                    onNavigateToProfile = {},
                    onNavigateToCart = {},
                    onNavigateToLogin = {},
                    onNavigateToAddresses = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(SettingsTestTags.currency(AppCurrency.EUR.name)).performClick()
        composeTestRule.onNodeWithText("Dark").performClick()
        composeTestRule.onNodeWithTag(SettingsTestTags.SyncButton).performScrollTo().performClick()

        assertEquals(
            listOf(
                SettingsIntent.ChangeCurrency(AppCurrency.EUR),
                SettingsIntent.ChangeTheme(AppTheme.DARK),
                SettingsIntent.RequestFirebaseSync,
            ),
            intents,
        )
    }

    @Test
    fun signedInSettingsShowsLogoutAndSavedAddressesNavigation() {
        val intents = mutableListOf<SettingsIntent>()
        var addressClicks = 0

        composeTestRule.setContent {
            ShopzenTheme {
                SettingsContent(
                    state = SettingsState(isGuest = false),
                    onIntent = { intents += it },
                    onNavigateToDiscover = {},
                    onNavigateToSearch = {},
                    onNavigateToWishlist = {},
                    onNavigateToProfile = {},
                    onNavigateToCart = {},
                    onNavigateToLogin = {},
                    onNavigateToAddresses = { addressClicks++ },
                )
            }
        }

        composeTestRule.onAllNodesWithTag(SettingsTestTags.LoginButton).assertCountEquals(0)
        composeTestRule.onNodeWithTag(SettingsTestTags.LogoutButton).performScrollTo().performClick()
        composeTestRule.onNodeWithTag(SettingsTestTags.AddressesButton).performScrollTo().performClick()

        assertEquals(listOf(SettingsIntent.RequestLogout), intents)
        assertEquals(1, addressClicks)
    }

    @Test
    fun authRequiredDialogPreservesLoginAction() {
        var loginClicks = 0

        composeTestRule.setContent {
            ShopzenTheme {
                SettingsContent(
                    state = SettingsState(
                        isGuest = false,
                        showAuthRequiredDialog = true,
                    ),
                    onIntent = {},
                    onNavigateToDiscover = {},
                    onNavigateToSearch = {},
                    onNavigateToWishlist = {},
                    onNavigateToProfile = {},
                    onNavigateToCart = {},
                    onNavigateToLogin = { loginClicks++ },
                    onNavigateToAddresses = {},
                )
            }
        }

        composeTestRule.onNodeWithText("This feature requires authentication. Please log in to continue.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").performClick()

        assertEquals(1, loginClicks)
    }

    @Test
    fun logoutDialogPreservesConfirmAction() {
        val intents = mutableListOf<SettingsIntent>()

        composeTestRule.setContent {
            ShopzenTheme {
                SettingsContent(
                    state = SettingsState(
                        isGuest = true,
                        showLogoutDialog = true,
                    ),
                    onIntent = { intents += it },
                    onNavigateToDiscover = {},
                    onNavigateToSearch = {},
                    onNavigateToWishlist = {},
                    onNavigateToProfile = {},
                    onNavigateToCart = {},
                    onNavigateToLogin = {},
                    onNavigateToAddresses = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Are you sure you want to log out?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logout").performClick()

        assertEquals(listOf(SettingsIntent.ConfirmLogout), intents)
    }
}
