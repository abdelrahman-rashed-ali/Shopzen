package iti.data.product.repository

import iti.data.product.mapper.toDomain
import iti.data.product.remote.ProductRemoteDataSource
import iti.domain.product.model.Product
import iti.domain.product.repository.ProductRepository

/**
 * Concrete [ProductRepository] implementation.
 * Delegates to [ProductRemoteDataSource] and maps the DTO to the domain model.
 */
class ProductRepositoryImpl(
    private val remoteDataSource: ProductRemoteDataSource,
) : ProductRepository {

    override suspend fun getProductById(id: Long): Result<Product> {
        return runCatching {
            remoteDataSource.getProductById(id).product.toDomain()
        }
    }
}
