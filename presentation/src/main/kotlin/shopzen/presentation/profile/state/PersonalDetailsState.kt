package shopzen.presentation.profile.state

data class PersonalDetailsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGuest: Boolean = true,
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = null,
    val nameError: String? = null,
    val isSaved: Boolean = false
)
