package shopzen.domain.profile.usecase

import shopzen.domain.profile.model.UserProfile
import shopzen.domain.profile.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    operator fun invoke(uid: String): Flow<Result<UserProfile>> =
        profileRepository.getUserProfile(uid)
}
