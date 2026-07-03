package shopzen.data.profile.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val USERS = "users"
private const val ADDRESSES = "addresses"
private const val PREFERENCES_FIELD = "preferences"
private const val SHOPIFY_CUSTOMER_ID_FIELD = "shopifyCustomerId"

/**
 * Firestore CRUD against users/{uid} and users/{uid}/addresses.
 * DocumentSnapshot / QuerySnapshot never leave this class unmapped —
 * everything is exposed as plain Map<String, Any?> for ProfileMapper.
 */
class RemoteProfileDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : RemoteProfileDataSource {

    private fun userDoc(uid: String) = firestore.collection(USERS).document(uid)
    private fun addressesCollection(uid: String) = userDoc(uid).collection(ADDRESSES)

    override fun getUserProfile(uid: String): Flow<Result<Map<String, Any?>>> = callbackFlow {
        val registration = userDoc(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val data = snapshot?.data.orEmpty() + mapOf("uid" to uid)
            trySend(Result.success(data))
        }
        awaitClose { registration.remove() }
    }

    override suspend fun updateUserProfile(uid: String, data: Map<String, Any?>): Result<Unit> = runCatching {
        userDoc(uid).set(data, SetOptions.merge()).await()
        Unit
    }

    override fun getAddresses(uid: String): Flow<Result<List<Map<String, Any?>>>> = callbackFlow {
        val registration = addressesCollection(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val list = snapshot?.documents.orEmpty().map { it.data.orEmpty() + mapOf("id" to it.id) }
            trySend(Result.success(list))
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addAddress(uid: String, data: Map<String, Any?>): Result<Unit> = runCatching {
        addressesCollection(uid).add(data).await()
        Unit
    }

    override suspend fun updateAddress(uid: String, addressId: String, data: Map<String, Any?>): Result<Unit> =
        runCatching {
            addressesCollection(uid).document(addressId).set(data, SetOptions.merge()).await()
            Unit
        }

    override suspend fun deleteAddress(uid: String, addressId: String): Result<Unit> = runCatching {
        addressesCollection(uid).document(addressId).delete().await()
        Unit
    }

    override suspend fun setDefaultAddress(uid: String, addressId: String): Result<Unit> = runCatching {
        val snapshot = addressesCollection(uid).get().await()
        firestore.runBatch { batch ->
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isDefault", doc.id == addressId)
            }
        }.await()
        Unit
    }

    override suspend fun writePreferences(uid: String, data: Map<String, Any?>): Result<Unit> = runCatching {
        userDoc(uid).set(mapOf(PREFERENCES_FIELD to data), SetOptions.merge()).await()
        Unit
    }

    override suspend fun readPreferences(uid: String): Result<Map<String, Any?>?> = runCatching {
        @Suppress("UNCHECKED_CAST")
        userDoc(uid).get().await().get(PREFERENCES_FIELD) as? Map<String, Any?>
    }

    // ── Shopify customer ID ───────────────────────────────────────────────────

    override suspend fun saveShopifyCustomerId(uid: String, customerId: Long): Result<Unit> =
        runCatching {
            userDoc(uid)
                .set(mapOf(SHOPIFY_CUSTOMER_ID_FIELD to customerId), SetOptions.merge())
                .await()
            Unit
        }

    override suspend fun getShopifyCustomerId(uid: String): Result<Long?> = runCatching {
        userDoc(uid).get().await().getLong(SHOPIFY_CUSTOMER_ID_FIELD)
    }
}
