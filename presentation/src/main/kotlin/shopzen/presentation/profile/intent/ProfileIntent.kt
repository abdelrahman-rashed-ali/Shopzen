package shopzen.presentation.profile.intent

import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.model.AppTheme

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data class LanguageSelected(val language: AppLanguage) : ProfileIntent
    data class ThemeSelected(val theme: AppTheme) : ProfileIntent
    data class CurrencySelected(val currency: AppCurrency) : ProfileIntent
    data object PersonalDetailsClicked : ProfileIntent
    data object SavedAddressesClicked : ProfileIntent
    data object NavigationHandled : ProfileIntent
    data object DismissAuthRequiredDialog : ProfileIntent
    data object SignOutClicked : ProfileIntent
    data object ConfirmSignOut : ProfileIntent
    data object DismissLogoutDialog : ProfileIntent
}
