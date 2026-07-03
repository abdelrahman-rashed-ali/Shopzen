package shopzen.data.wishlist.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_items")
data class WishlistEntity(
    @PrimaryKey
    val id: String,
    val productId: String,
    val title: String,
    val vendor: String,
    val price: String,
    val imageUrl: String,
    val userId: String,
    val addedAt: Long
)
