package shopzen.domain.profile.model

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = null,
)
