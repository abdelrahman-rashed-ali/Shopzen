package shopzen.domain.product.usecase

import javax.inject.Inject
import shopzen.domain.product.model.ProductComparisonData
import shopzen.domain.product.model.ProductComparisonItem
import shopzen.domain.product.model.ProductVariantComparisonItem

class CompareProductsUseCase @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase
) {
    suspend operator fun invoke(
        productAId: String,
        productBId: String,
        productAVariantId: String?,
        productBVariantId: String?
    ): Result<ProductComparisonData> {
        val idA = productAId.toLongOrNull() ?: return Result.failure(IllegalArgumentException("Invalid Product A ID"))
        val idB = productBId.toLongOrNull() ?: return Result.failure(IllegalArgumentException("Invalid Product B ID"))

        val productA = getProductByIdUseCase(idA).getOrNull() ?: return Result.failure(Exception("Product A not found"))
        val productB = getProductByIdUseCase(idB).getOrNull() ?: return Result.failure(Exception("Product B not found"))

        fun mapProduct(p: shopzen.domain.product.model.Product, variantId: String?): ProductComparisonItem {
            val variants = p.variants.map { v ->
                ProductVariantComparisonItem(
                    id = v.id.toString(),
                    title = v.title,
                    price = v.price.toDoubleOrNull() ?: 0.0,
                    available = v.inventoryQuantity > 0
                )
            }
            val price = if (variantId != null) {
                p.variants.firstOrNull { it.id.toString() == variantId }?.price?.toDoubleOrNull()
            } else {
                p.price.toDoubleOrNull()
            } ?: 0.0

            return ProductComparisonItem(
                id = p.id.toString(),
                title = p.title,
                description = p.description,
                price = price,
                currency = "EGP", // Hardcoded or from variant
                imageUrl = p.images.firstOrNull()?.src,
                available = p.variants.any { it.inventoryQuantity > 0 },
                tags = p.tags,
                variants = variants,
                rating = null, // Backend not supplying yet
                reviewCount = null
            )
        }

        return Result.success(
            ProductComparisonData(
                productA = mapProduct(productA, productAVariantId),
                productB = mapProduct(productB, productBVariantId)
            )
        )
    }
}
