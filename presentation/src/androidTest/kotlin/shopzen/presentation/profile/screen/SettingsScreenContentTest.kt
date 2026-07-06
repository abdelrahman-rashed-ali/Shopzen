package shopzen.presentation.profile.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import shopzen.presentation.profile.intent.SettingsIntent
import shopzen.presentation.profile.state.SettingsState
import shopzen.presentation.theme.ShopzenTheme

class SettingsScreenContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun guestSettingsShowsShellAndLoginAction() {
        composeTestRule.setContent {
            ShopzenTheme {
                SettingsContent(
                    state = SettingsState(isGuest = true),
                    onIntent = { _: SettingsIntent -> },
                    onNavigateToDiscover = {},
                    onNavigateToSearch = {},
                    onNavigateToWishlist = {},
                    onNavigateToProfile = {},
                    onNavigateToCart = {},
                    onNavigateToLogin = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Cart").assertIsDisplayed()
    }
}
