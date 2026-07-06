package shopzen.app.di.firebase

import com.google.firebase.auth.FirebaseAuth
import shopzen.data.auth.remote.RemoteAuthDataSource
import shopzen.data.auth.remote.RemoteAuthDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {
    companion object {

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

    }

    @Binds
    @Singleton
    abstract fun bindRemoteAuthDataSource(
        impl: RemoteAuthDataSourceImpl
    ): RemoteAuthDataSource
}
