package com.shopzen.data.auth.mapper

import com.google.firebase.auth.FirebaseUser
import com.shopzen.domain.auth.model.User

fun FirebaseUser.toDomainModel(): User = User(
    uid = uid,
    email = email.orEmpty(),
    displayName = displayName.orEmpty(),
    photoUrl = photoUrl?.toString(),
    isEmailVerified = isEmailVerified
)
