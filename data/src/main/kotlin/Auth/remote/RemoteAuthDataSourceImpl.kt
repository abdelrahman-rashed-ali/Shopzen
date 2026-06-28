package com.shopzen.data.auth.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteAuthDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : RemoteAuthDataSource {

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): FirebaseUser {

        val result = firebaseAuth
            .createUserWithEmailAndPassword(
                email,
                password
            )
            .await()

        val user = requireNotNull(result.user) {
            "Firebase returned a null user after registration"
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates)
            .await()
        user.sendEmailVerification()
            .await()
        return user

    }

    override suspend fun sendVerificationEmail() {
        val user = requireNotNull(firebaseAuth.currentUser) {
            "No authenticated user to send verification email to"
        }
        user.sendEmailVerification().await()
    }
}
