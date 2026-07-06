package shopzen.domain.ads.usecase

import shopzen.domain.ads.model.Ad
import shopzen.domain.ads.repository.AdRepository
import javax.inject.Inject

class GetAdsUseCase @Inject constructor(
    private val repository: AdRepository
) {
    suspend operator fun invoke(): Result<List<Ad>> = repository.getAds()
}
