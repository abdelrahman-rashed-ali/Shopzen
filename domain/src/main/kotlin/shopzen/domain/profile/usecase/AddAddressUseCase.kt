package shopzen.domain.profile.usecase

import shopzen.domain.profile.model.Address
import shopzen.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class AddAddressUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(uid: String, address: Address): Result<Unit> =
        profileRepository.addAddress(uid, address)
}
