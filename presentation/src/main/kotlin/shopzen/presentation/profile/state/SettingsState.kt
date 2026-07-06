package shopzen.presentation.profile.state

import shopzen.domain.profile.model.UserPreferences

data class SettingsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val preferences: UserPreferences = UserPreferences(),
    val isGuest: Boolean = true,
    val showLogoutDialog: Boolean = false,
    val showAuthRequiredDialog: Boolean = false,
)
