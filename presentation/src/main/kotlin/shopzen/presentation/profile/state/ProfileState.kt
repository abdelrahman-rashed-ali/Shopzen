package shopzen.presentation.profile.state

import shopzen.domain.profile.model.UserPreferences
import shopzen.domain.profile.model.UserProfile

data class ProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profile: UserProfile? = null,
    val preferences: UserPreferences = UserPreferences(),
    val isGuest: Boolean = true,
    val showLogoutDialog: Boolean = false,
    val showAuthRequiredDialog: Boolean = false,
    val navigationTarget: ProfileNavigationTarget? = null,
    val isSignedOut: Boolean = false
)

enum class ProfileNavigationTarget {
    PERSONAL_DETAILS,
    SAVED_LOCATIONS
}
