package shopzen.presentation.profile.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import shopzen.presentation.profile.intent.AddressEditIntent
import shopzen.presentation.profile.state.AddressEditState
import shopzen.presentation.profile.state.AddressEntryMode
import shopzen.presentation.profile.state.AddressPlaceSuggestion
import shopzen.presentation.common.theme.ShopzenTheme

class AddEditAddressScreenContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun assistedModeShowsSearchMapContactAddressAndSave() {
        composeRule.setContent {
            ShopzenTheme {
                AddressEditContent(
                    state = sampleState(),
                    onIntent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithTag(AddressEditTestTags.ModeSwitcher).assertIsDisplayed()
        composeRule.onNodeWithTag(AddressEditTestTags.AssistedSection).assertIsDisplayed()
        composeRule.onNodeWithTag(AddressEditTestTags.SearchField).assertIsDisplayed()
        composeRule.onNodeWithTag(AddressEditTestTags.MapPreview).assertIsDisplayed()
        composeRule.onNodeWithTag(AddressEditTestTags.ContactSection).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(AddressEditTestTags.AddressSection).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(AddressEditTestTags.SaveButton).assertIsDisplayed()
    }

    @Test
    fun assistedModeWiresSearchSuggestionMapPinAndSaveIntents() {
        val suggestion = sampleSuggestion()
        val intents = mutableListOf<AddressEditIntent>()

        composeRule.setContent {
            ShopzenTheme {
                AddressEditContent(
                    state = sampleState(
                        placeSuggestions = listOf(suggestion),
                    ),
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithTag(AddressEditTestTags.SearchField).performTextInput("cairo")
        composeRule.onNodeWithTag(AddressEditTestTags.suggestion(suggestion.id)).performClick()
        composeRule.onNodeWithTag(AddressEditTestTags.UseMapPinButton).performScrollTo().performClick()
        composeRule.onNodeWithTag(AddressEditTestTags.SaveButton).performClick()

        assertEquals(
            listOf(
                AddressEditIntent.PlaceQueryChanged("cairo"),
                AddressEditIntent.PlaceSuggestionSelected(suggestion),
                AddressEditIntent.UseMapPin,
                AddressEditIntent.Save,
            ),
            intents,
        )
    }

    @Test
    fun manualModeShowsFallbackAndEditableFields() {
        val intents = mutableListOf<AddressEditIntent>()

        composeRule.setContent {
            ShopzenTheme {
                AddressEditContent(
                    state = sampleState(entryMode = AddressEntryMode.MANUAL),
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithTag(AddressEditTestTags.ManualNotice).assertIsDisplayed()
        composeRule.onNodeWithText("Manual Fallback").assertIsDisplayed()
        composeRule.onNodeWithText("First name *").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Address line 1 *").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Map Assist").performClick()

        assertEquals(listOf(AddressEditIntent.EntryModeChanged(AddressEntryMode.ASSISTED)), intents)
    }

    @Test
    fun autofillAndValidationMessagesStayInline() {
        composeRule.setContent {
            ShopzenTheme {
                AddressEditContent(
                    state = sampleState(
                        isAutofilled = true,
                        hasManualOverride = true,
                        fieldError = "Please fill in all required fields",
                    ),
                    onIntent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithTag(AddressEditTestTags.AutofillStatus).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Manual edits detected. Your typed values will be saved.").assertIsDisplayed()
        composeRule.onNodeWithTag(AddressEditTestTags.FieldError).performScrollTo().assertIsDisplayed()
    }

    private fun sampleState(
        entryMode: AddressEntryMode = AddressEntryMode.ASSISTED,
        placeSuggestions: List<AddressPlaceSuggestion> = emptyList(),
        isAutofilled: Boolean = false,
        hasManualOverride: Boolean = false,
        fieldError: String? = null,
    ) = AddressEditState(
        isGuest = false,
        entryMode = entryMode,
        placeQuery = "",
        placeSuggestions = placeSuggestions,
        selectedLatitude = 30.0444,
        selectedLongitude = 31.2357,
        isAutofilled = isAutofilled,
        hasManualOverride = hasManualOverride,
        label = "Home",
        firstName = "Jane",
        lastName = "Doe",
        recipientName = "Jane Doe",
        addressLine1 = "Talaat Harb St",
        city = "Cairo",
        stateOrProvince = "Cairo Governorate",
        postalCode = "11511",
        country = "Egypt",
        phone = "+201000000000",
        fieldError = fieldError,
    )

    private fun sampleSuggestion() = AddressPlaceSuggestion(
        id = "suggestion-1",
        title = "Downtown Cairo",
        subtitle = "Talaat Harb St, Cairo",
        addressLine1 = "Talaat Harb St",
        city = "Cairo",
        stateOrProvince = "Cairo Governorate",
        postalCode = "11511",
        country = "Egypt",
        latitude = 30.0444,
        longitude = 31.2357,
    )
}
