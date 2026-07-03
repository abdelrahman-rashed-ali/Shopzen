package shopzen.domain.profile.usecase

import shopzen.domain.profile.model.Address
import shopzen.domain.profile.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedAddressesUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    operator fun invoke(uid: String): Flow<Result<List<Address>>> =
        profileRepository.getSavedAddresses(uid)
}
