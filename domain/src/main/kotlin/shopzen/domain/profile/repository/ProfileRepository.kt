package shopzen.domain.profile.repository

import shopzen.domain.profile.model.Address
import shopzen.domain.profile.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(uid: String): Flow<Result<UserProfile>>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>

    fun getSavedAddresses(uid: String): Flow<Result<List<Address>>>
    suspend fun addAddress(uid: String, address: Address): Result<Unit>
    suspend fun updateAddress(uid: String, address: Address): Result<Unit>
    suspend fun deleteAddress(uid: String, addressId: String): Result<Unit>
    suspend fun setDefaultAddress(uid: String, addressId: String): Result<Unit>
}
