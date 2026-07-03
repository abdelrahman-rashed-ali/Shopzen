package shopzen.presentation.profile.intent

sealed interface PersonalDetailsIntent {
    data class NameChanged(val value: String) : PersonalDetailsIntent
    data class PhoneChanged(val value: String) : PersonalDetailsIntent
    data class PhotoChanged(val value: String?) : PersonalDetailsIntent
    data object Submit : PersonalDetailsIntent
}
