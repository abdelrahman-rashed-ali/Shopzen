package shopzen.domain.onboarding.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun hasCompletedOnboarding(): Flow<Boolean>
    suspend fun completeOnboarding()
}
