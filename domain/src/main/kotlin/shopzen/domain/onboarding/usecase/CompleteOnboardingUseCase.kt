package shopzen.domain.onboarding.usecase

import shopzen.domain.onboarding.repository.OnboardingRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke(): Unit {
        onboardingRepository.completeOnboarding()
    }
}
