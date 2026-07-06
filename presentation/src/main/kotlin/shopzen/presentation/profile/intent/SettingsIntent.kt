package shopzen.presentation.profile.intent

import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.model.AppTheme

sealed class SettingsIntent {
    data object LoadSettings : SettingsIntent()
    data class ChangeCurrency(val currency: AppCurrency) : SettingsIntent()
    data class ChangeLanguage(val language: AppLanguage) : SettingsIntent()
    data class ChangeTheme(val theme: AppTheme) : SettingsIntent()
    data object RequestFirebaseSync : SettingsIntent()
    data object RequestLogout : SettingsIntent()
    data object ConfirmLogout : SettingsIntent()
    data object DismissDialog : SettingsIntent()
}
