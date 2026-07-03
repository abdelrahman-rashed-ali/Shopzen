package shopzen.presentation.profile.state

data class PersonalDetailsState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
)
