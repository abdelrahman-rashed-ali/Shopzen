package shopzen.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import shopzen.data.database.ShopzenDatabase
import shopzen.data.wishlist.local.dao.WishlistDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ShopzenDatabase {
        return Room.databaseBuilder(
            context,
            ShopzenDatabase::class.java,
            "shopzen_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWishlistDao(
        database: ShopzenDatabase
    ): WishlistDao {
        return database.wishlistDao()
    }
}
