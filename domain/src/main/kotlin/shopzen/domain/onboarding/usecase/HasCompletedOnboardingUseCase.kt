package shopzen.domain.onboarding.usecase

import shopzen.domain.onboarding.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HasCompletedOnboardingUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return onboardingRepository.hasCompletedOnboarding()
    }
}
