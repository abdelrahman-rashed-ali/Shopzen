package shopzen.data.profile.remote

import kotlinx.coroutines.flow.Flow

interface RemoteProfileDataSource {
    fun getUserProfile(uid: String): Flow<Result<Map<String, Any?>>>
    suspend fun updateUserProfile(uid: String, data: Map<String, Any?>): Result<Unit>

    fun getAddresses(uid: String): Flow<Result<List<Map<String, Any?>>>>
    suspend fun addAddress(uid: String, data: Map<String, Any?>): Result<Unit>
    suspend fun updateAddress(uid: String, addressId: String, data: Map<String, Any?>): Result<Unit>
    suspend fun deleteAddress(uid: String, addressId: String): Result<Unit>
    suspend fun setDefaultAddress(uid: String, addressId: String): Result<Unit>

    suspend fun writePreferences(uid: String, data: Map<String, Any?>): Result<Unit>
    suspend fun readPreferences(uid: String): Result<Map<String, Any?>?>
}
