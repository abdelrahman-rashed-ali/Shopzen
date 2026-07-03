package shopzen.domain.profile.usecase

import shopzen.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class SetDefaultAddressUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(uid: String, addressId: String): Result<Unit> =
        profileRepository.setDefaultAddress(uid, addressId)
}
