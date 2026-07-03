package shopzen.presentation.profile.intent

sealed class PersonalDetailsIntent {
    data object LoadDetails : PersonalDetailsIntent()
    data class FullNameChanged(val value: String) : PersonalDetailsIntent()
    data class PhoneChanged(val value: String) : PersonalDetailsIntent()
    data object Save : PersonalDetailsIntent()
}
