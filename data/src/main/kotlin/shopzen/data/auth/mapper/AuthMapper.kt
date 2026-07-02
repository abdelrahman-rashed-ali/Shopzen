package shopzen.data.auth.mapper

import com.google.firebase.auth.FirebaseUser
import shopzen.domain.auth.model.AuthSession
import shopzen.domain.auth.model.User

fun FirebaseUser.toDomainModel(): User = User(
    uid = uid,
    email = email.orEmpty(),
    displayName = displayName.orEmpty(),
    photoUrl = photoUrl?.toString(),
    isEmailVerified = isEmailVerified
)

fun FirebaseUser?.toGuestSession(): AuthSession = AuthSession(
    user = this?.toDomainModel(),
    isGuest = this?.isAnonymous ?: true
)
