package shopzen.domain.profile.usecase

import shopzen.domain.profile.model.UserProfile
import shopzen.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(profile: UserProfile): Result<Unit> =
        profileRepository.updateUserProfile(profile)
}
