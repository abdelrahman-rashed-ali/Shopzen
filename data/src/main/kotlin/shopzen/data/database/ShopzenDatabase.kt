package shopzen.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import shopzen.data.wishlist.local.entity.WishlistEntity
import shopzen.data.wishlist.local.dao.WishlistDao

@Database(
    entities = [WishlistEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ShopzenDatabase : RoomDatabase() {
    abstract fun wishlistDao(): WishlistDao
}
