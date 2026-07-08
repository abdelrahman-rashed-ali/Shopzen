package shopzen.data.ads.repository

import shopzen.data.ads.remote.RemoteAdDataSource
import shopzen.domain.ads.model.Ad
import shopzen.domain.ads.repository.AdRepository
import javax.inject.Inject

class AdRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteAdDataSource
) : AdRepository {
    override suspend fun getAds(): Result<List<Ad>> {
        return try {
            Result.success(remoteDataSource.getAds())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
