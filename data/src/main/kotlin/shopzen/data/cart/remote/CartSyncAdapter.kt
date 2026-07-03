package shopzen.data.cart.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Adapter for syncing the Shopify Cart ID across devices via Firestore.
 * 
 * We store a document under `users/{userId}/cart/default` that contains
 * the current `cartId`. This ensures that when a user logs in on a new device,
 * or clears their app data, we can recover their existing Shopify cart rather
 * than creating a new one.
 */
class CartSyncAdapter @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val CART_COLLECTION = "cart"
        private const val DEFAULT_CART_DOC = "default"
        private const val FIELD_CART_ID = "cartId"
    }

    /**
     * Retrieves the synchronized cart ID for the user, or null if they don't have one.
     */
    suspend fun getCartId(userId: String): String? {
        return try {
            val snapshot = firestore
                .collection(USERS_COLLECTION).document(userId)
                .collection(CART_COLLECTION).document(DEFAULT_CART_DOC)
                .get()
                .await()

            snapshot.getString(FIELD_CART_ID)
        } catch (e: Exception) {
            // Log error, but don't crash. Fallback to null (which means no synced cart found).
            null
        }
    }

    /**
     * Saves the cart ID to Firestore so it can be retrieved on other devices.
     */
    suspend fun saveCartId(userId: String, cartId: String) {
        try {
            val data = mapOf(FIELD_CART_ID to cartId)
            firestore
                .collection(USERS_COLLECTION).document(userId)
                .collection(CART_COLLECTION).document(DEFAULT_CART_DOC)
                .set(data)
                .await()
        } catch (e: Exception) {
            // We failed to sync the cart ID. It will exist locally, but not across devices.
        }
    }
}
