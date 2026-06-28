package iti.data.catalog.mapper

import iti.data.catalog.remote.dto.CollectionDto
import iti.data.catalog.remote.dto.ProductDto
import iti.domain.catalog.model.Brand
import iti.domain.catalog.model.Category
import iti.domain.catalog.model.Product

/**
 * Maps data-layer DTOs to domain models.
 * All mapping happens here — DTOs never leave the :data module unmapped.
 */
object CatalogMapper {

    /**
     * Maps a [ProductDto] to a domain [Product].
     */
    fun ProductDto.toDomain(): Product {
        val primaryImage = images?.firstOrNull()?.src.orEmpty()
        val allImages = images?.mapNotNull { it.src }.orEmpty()
        val defaultPrice = variants?.firstOrNull()?.price.orEmpty()

        return Product(
            id = id.toString(),
            title = title.orEmpty(),
            vendor = vendor.orEmpty(),
            productType = productType.orEmpty(),
            price = defaultPrice,
            imageUrl = primaryImage,
            images = allImages,
            createdAt = createdAt.orEmpty()
        )
    }

    /**
     * Maps a [CollectionDto] to a domain [Category].
     */
    fun CollectionDto.toDomain(): Category {
        return Category(
            id = id.toString(),
            title = title.orEmpty(),
            imageUrl = image?.src
        )
    }

    /**
     * Extracts distinct [Brand] instances from a list of [ProductDto].
     */
    fun List<ProductDto>.toDistinctBrands(): List<Brand> {
        return this
            .mapNotNull { it.vendor }
            .distinct()
            .map { vendorName -> Brand(name = vendorName) }
    }
}
