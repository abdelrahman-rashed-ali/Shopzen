package shopzen.presentation.profile.state

import shopzen.domain.profile.model.UserPreferences
import shopzen.domain.profile.model.UserProfile

data class ProfileState(
    val profile: UserProfile? = null,
    val preferences: UserPreferences = UserPreferences(),
    val isGuest: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showLogoutDialog: Boolean = false,
    val showAuthRequiredDialog: Boolean = false,
    val navigationTarget: ProfileNavigationTarget? = null,
)

enum class ProfileNavigationTarget {
    PERSONAL_DETAILS,
    SAVED_ADDRESSES,
}
