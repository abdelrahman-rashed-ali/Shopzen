package shopzen.data.onboarding.repository

import shopzen.data.onboarding.local.LocalOnboardingDataSource
import shopzen.domain.onboarding.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val localDataSource: LocalOnboardingDataSource
) : OnboardingRepository {
    
    override fun hasCompletedOnboarding(): Flow<Boolean> {
        return localDataSource.hasCompleted
    }

    override suspend fun completeOnboarding() {
        localDataSource.completeOnboarding()
    }
}
