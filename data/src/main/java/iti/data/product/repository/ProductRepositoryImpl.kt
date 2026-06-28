package iti.data.product.repository

import iti.data.product.mapper.toDomain
import iti.data.product.remote.ProductRemoteDataSource
import iti.domain.product.model.Product
import iti.domain.product.repository.ProductRepository
import javax.inject.Inject

/**
 * Concrete [ProductRepository] implementation.
 * Delegates to [ProductRemoteDataSource] and maps the DTO to the domain model.
 */
class ProductRepositoryImpl  @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource,
) : ProductRepository {

    override suspend fun getProductById(id: Long): Result<Product> {
        return Result.success(remoteDataSource.getProductById(id).product.toDomain())
    }
}
