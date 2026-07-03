package shopzen.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import shopzen.data.cart.local.dao.CartDao
import shopzen.data.cart.local.entity.CartItemEntity

/**
 * Single Room database for Shopzen.
 *
 * Bump [version] and add a [androidx.room.migration.Migration] whenever the schema changes.
 * All DAOs are accessed exclusively through their corresponding [Local*DataSource] wrappers.
 */
@Database(
    entities  = [CartItemEntity::class],
    version   = 1,
    exportSchema = false,
)
abstract class ShopzenDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}
