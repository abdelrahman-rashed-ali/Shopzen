package shopzen.domain.profile.usecase

import shopzen.domain.profile.model.Address
import shopzen.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class UpdateAddressUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(uid: String, address: Address): Result<Unit> =
        profileRepository.updateAddress(uid, address)
}
