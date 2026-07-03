package shopzen.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import shopzen.data.cart.local.dao.CartDao
import shopzen.data.database.ShopzenDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShopzenDatabase =
        Room.databaseBuilder(
            context,
            ShopzenDatabase::class.java,
            "shopzen.db",
        ).build()

    @Provides
    @Singleton
    fun provideCartDao(db: ShopzenDatabase): CartDao = db.cartDao()
}
