package shopzen.data.onboarding.repository

import kotlinx.coroutines.flow.Flow
import shopzen.data.onboarding.local.LocalOnboardingDataSource
import shopzen.domain.onboarding.repository.OnboardingRepository
import javax.inject.Inject

class OnboardingRepositoryImpl @Inject constructor(
    private val localDataSource: LocalOnboardingDataSource
) : OnboardingRepository {
    
    override fun hasCompletedOnboarding(): Flow<Boolean> {
        return localDataSource.hasCompleted
    }

    override suspend fun completeOnboarding(): Unit {
        localDataSource.completeOnboarding()
    }
}
