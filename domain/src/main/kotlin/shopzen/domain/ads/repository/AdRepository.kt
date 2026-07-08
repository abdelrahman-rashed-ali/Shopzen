package shopzen.domain.ads.repository

import shopzen.domain.ads.model.Ad

interface AdRepository {
    suspend fun getAds(): Result<List<Ad>>
}
