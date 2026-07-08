package shopzen.presentation.profile.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import shopzen.domain.profile.model.UserProfile
import shopzen.presentation.profile.intent.ProfileIntent
import shopzen.presentation.profile.state.ProfileState
import shopzen.presentation.common.theme.ShopzenTheme

class ProfileScreenContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun signedInProfileShowsIdentityDetailsOrderHistoryAndNoAddresses() {
        composeRule.setContent {
            ShopzenTheme {
                ProfileContent(
                    state = ProfileState(
                        isGuest = false,
                        profile = sampleProfile(),
                    ),
                    onIntent = {},
                    onNavigateToLogin = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(ProfileTestTags.Hero).assertIsDisplayed()
        composeRule.onNodeWithText("JD").assertIsDisplayed()
        composeRule.onNodeWithText("Jane Doe").assertIsDisplayed()
        composeRule.onNodeWithText("jane@example.com").assertIsDisplayed()
        composeRule.onNodeWithTag(ProfileTestTags.Details).assertIsDisplayed()
        composeRule.onNodeWithText("********").assertIsDisplayed()
        composeRule.onNodeWithText("Edit Profile").assertIsDisplayed()
        composeRule.onNodeWithText("Order History").assertIsDisplayed()
        composeRule.onAllNodesWithText("Saved Locations").assertCountEquals(0)
    }

    @Test
    fun signedInProfileWiresEditOrderHistoryAndSignOutActions() {
        val intents = mutableListOf<ProfileIntent>()

        composeRule.setContent {
            ShopzenTheme {
                ProfileContent(
                    state = ProfileState(
                        isGuest = false,
                        profile = sampleProfile(),
                    ),
                    onIntent = { intents += it },
                    onNavigateToLogin = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(ProfileTestTags.EditButton).performClick()
        composeRule.onNodeWithTag(ProfileTestTags.OrderHistoryButton).performClick()
        composeRule.onNodeWithTag(ProfileTestTags.SignOutButton).performClick()

        assertEquals(
            listOf(
                ProfileIntent.PersonalDetailsClicked,
                ProfileIntent.OrderHistoryClicked,
                ProfileIntent.RequestSignOut,
            ),
            intents,
        )
    }

    @Test
    fun guestProfileShowsLoginFirstBranch() {
        var loginClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                ProfileContent(
                    state = ProfileState(isGuest = true),
                    onIntent = {},
                    onNavigateToLogin = { loginClicks++ },
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(ProfileTestTags.GuestCard).assertIsDisplayed()
        composeRule.onNodeWithText("Sign in to manage your profile").assertIsDisplayed()
        composeRule.onNodeWithTag(ProfileTestTags.LoginButton).performClick()
        composeRule.onAllNodesWithTag(ProfileTestTags.Details).assertCountEquals(0)

        assertEquals(1, loginClicks)
    }

    @Test
    fun logoutDialogConfirmAndDismissEmitExpectedIntents() {
        val intents = mutableListOf<ProfileIntent>()

        composeRule.setContent {
            ShopzenTheme {
                ProfileContent(
                    state = ProfileState(
                        isGuest = false,
                        profile = sampleProfile(),
                        showLogoutDialog = true,
                    ),
                    onIntent = { intents += it },
                    onNavigateToLogin = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Are you sure you want to log out?").assertIsDisplayed()
        composeRule.onNodeWithText("Sign Out").performClick()

        assertEquals(listOf(ProfileIntent.ConfirmSignOut), intents)
    }

    @Test
    fun profileInitialsUseFirstAndLastName() {
        assertEquals("JD", profileInitials(fullName = "Jane Doe", email = "jane@example.com"))
        assertEquals("M", profileInitials(fullName = "Mona", email = "mona@example.com"))
        assertEquals("S", profileInitials(fullName = "", email = "shopzen@example.com"))
    }

    private fun sampleProfile() = UserProfile(
        uid = "user-1",
        fullName = "Jane Doe",
        email = "jane@example.com",
        phone = "+201000000000",
        photoUrl = null,
    )
}
