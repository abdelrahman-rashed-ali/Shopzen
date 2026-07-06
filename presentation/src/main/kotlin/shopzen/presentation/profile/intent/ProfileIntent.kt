package shopzen.presentation.profile.intent

sealed class ProfileIntent {
    data object LoadProfile : ProfileIntent()
    data object PersonalDetailsClicked : ProfileIntent()
    data object SavedLocationsClicked : ProfileIntent()
    data object OrderHistoryClicked : ProfileIntent()
    data object RequestSignOut : ProfileIntent()
    data object ConfirmSignOut : ProfileIntent()
    data object DismissDialog : ProfileIntent()
    data object NavigationHandled : ProfileIntent()
}
