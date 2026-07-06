package shopzen.presentation.profile.intent

import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.model.AppTheme

sealed class ProfileIntent {
    data object LoadProfile : ProfileIntent()
    data class ChangeCurrency(val currency: AppCurrency) : ProfileIntent()
    data class ChangeLanguage(val language: AppLanguage) : ProfileIntent()
    data class ChangeTheme(val theme: AppTheme) : ProfileIntent()
    data object PersonalDetailsClicked : ProfileIntent()
    data object SavedLocationsClicked : ProfileIntent()
    data object RequestSignOut : ProfileIntent()
    data object ConfirmSignOut : ProfileIntent()
    data object DismissDialog : ProfileIntent()
    data object NavigationHandled : ProfileIntent()
}
