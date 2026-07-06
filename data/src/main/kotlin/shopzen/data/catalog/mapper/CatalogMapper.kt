package shopzen.data.catalog.mapper

import shopzen.data.catalog.remote.dto.CollectionDto
import shopzen.data.catalog.remote.dto.ProductDto
import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product

object CatalogMapper {

    fun ProductDto.toDomain(): Product {
        val primaryImage = images?.firstOrNull()?.src.orEmpty()
        val basePrice = variants?.firstOrNull()?.price.orEmpty()

        return Product(
            id = id.toString(),
            title = title.orEmpty(),
            vendor = vendor.orEmpty(),
            productType = productType.orEmpty(),
            price = basePrice,
            imageUrl = primaryImage
        )
    }

    fun CollectionDto.toDomain(): Category {
        return Category(
            id = id.toString(),
            title = title.orEmpty(),
            imageUrl = image?.src.orEmpty()
        )
    }

    fun List<ProductDto>.toDistinctBrands(): List<Brand> {
        return this
            .groupBy { it.vendor.orEmpty() }
            .filter { it.key.isNotEmpty() }
            .map { (vendor, products) ->
                Brand(
                    name = vendor,
                    imageUrl = products.firstOrNull()?.images?.firstOrNull()?.src.orEmpty()
                )
            }
    }
}
