package shopzen.data.wishlist.mapper

import shopzen.data.wishlist.local.entity.WishlistEntity
import shopzen.domain.wishlist.model.WishlistItem

object WishlistMapper {
    fun WishlistEntity.toDomain(): WishlistItem {
        return WishlistItem(
            id = id,
            productId = productId,
            title = title,
            vendor = vendor,
            price = price,
            imageUrl = imageUrl,
            userId = userId,
            addedAt = addedAt
        )
    }

    fun WishlistItem.toEntity(): WishlistEntity {
        return WishlistEntity(
            id = id,
            productId = productId,
            title = title,
            vendor = vendor,
            price = price,
            imageUrl = imageUrl,
            userId = userId,
            addedAt = addedAt
        )
    }
}
