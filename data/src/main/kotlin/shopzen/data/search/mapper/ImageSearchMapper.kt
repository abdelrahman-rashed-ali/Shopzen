package shopzen.data.search.mapper

import shopzen.data.search.remote.dto.ImageSearchProductDto
import shopzen.domain.catalog.model.Product

object ImageSearchMapper {
    fun mapToDomain(dto: ImageSearchProductDto): Product {
        return Product(
            id = dto.id,
            title = dto.title,
            vendor = dto.vendor,
            productType = dto.productType,
            price = dto.price,
            imageUrl = dto.featuredImage ?: ""
        )
    }
}
