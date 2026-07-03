package shopzen.domain.profile.usecase

import shopzen.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class DeleteAddressUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(uid: String, addressId: String): Result<Unit> =
        profileRepository.deleteAddress(uid, addressId)
}
