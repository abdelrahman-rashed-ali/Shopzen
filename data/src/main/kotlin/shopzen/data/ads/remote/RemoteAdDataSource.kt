package shopzen.data.ads.remote

import kotlinx.coroutines.delay
import shopzen.domain.ads.model.Ad

class RemoteAdDataSource {
    suspend fun getAds(): List<Ad> {
        delay(600) // Simulating network latency
        return listOf(
            Ad(
                id = "ad_1",
                title = "SUMMER CHRONOGRAPHS",
                description = "Experience classic horology redefined. Sleek design combined with perpetual precision.",
                imageUrl = "https://images.unsplash.com/photo-1547996160-81dfa63595aa?q=80&w=1200",
                ctaText = "EXPLORE WATCHES"
            ),
            Ad(
                id = "ad_2",
                title = "ETHEREAL DIAMONDS",
                description = "Masterfully cut rings and necklaces reflecting the light of a thousand stars.",
                imageUrl = "https://images.unsplash.com/photo-1605100804763-247f67b3557e?q=80&w=1200",
                ctaText = "DISCOVER JEWELRY"
            ),
            Ad(
                id = "ad_3",
                title = "LA MAISON PARFUM",
                description = "A sophisticated blend of amber, bergamot, and rich woods. Find your new signature scent.",
                imageUrl = "https://images.unsplash.com/photo-1541643600914-78b084683601?q=80&w=1200",
                ctaText = "SHOP FRAGRANCES"
            ),
            Ad(
                id = "ad_4",
                title = "HANDCRAFTED COUTURE",
                description = "Exceptional Italian leather goods made to accompany you for a lifetime.",
                imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?q=80&w=1200",
                ctaText = "VIEW LEATHER GOODS"
            ),
            Ad(
                id = "ad_5",
                title = "THE GOLDEN HOUR",
                description = "Limited edition 18k gold bracelets and bangles. Elevate your everyday style.",
                imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?q=80&w=1200",
                ctaText = "BROWSE GOLD"
            )
        )
    }
}
