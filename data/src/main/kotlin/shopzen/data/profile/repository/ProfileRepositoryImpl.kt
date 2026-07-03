package shopzen.data.profile.repository

import shopzen.data.profile.mapper.toAddress
import shopzen.data.profile.mapper.toFirestoreMap
import shopzen.data.profile.mapper.toUserProfile
import shopzen.data.profile.remote.RemoteProfileDataSource
import shopzen.domain.profile.model.Address
import shopzen.domain.profile.model.UserProfile
import shopzen.domain.profile.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val remoteProfileDataSource: RemoteProfileDataSource,
) : ProfileRepository {

    override fun getUserProfile(uid: String): Flow<Result<UserProfile>> =
        remoteProfileDataSource.getUserProfile(uid).map { result ->
            result.mapCatching { it.toUserProfile() }
        }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> =
        remoteProfileDataSource.updateUserProfile(profile.uid, profile.toFirestoreMap())

    override fun getSavedAddresses(uid: String): Flow<Result<List<Address>>> =
        remoteProfileDataSource.getAddresses(uid).map { result ->
            result.mapCatching { list -> list.map { it.toAddress() } }
        }

    override suspend fun addAddress(uid: String, address: Address): Result<Unit> =
        remoteProfileDataSource.addAddress(uid, address.toFirestoreMap())

    override suspend fun updateAddress(uid: String, address: Address): Result<Unit> =
        remoteProfileDataSource.updateAddress(uid, address.id, address.toFirestoreMap())

    override suspend fun deleteAddress(uid: String, addressId: String): Result<Unit> =
        remoteProfileDataSource.deleteAddress(uid, addressId)

    override suspend fun setDefaultAddress(uid: String, addressId: String): Result<Unit> =
        remoteProfileDataSource.setDefaultAddress(uid, addressId)
}
