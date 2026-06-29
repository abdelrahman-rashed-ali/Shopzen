package shopzen.data.product.repository

import shopzen.data.product.mapper.toDomain
import shopzen.data.product.remote.ProductRemoteDataSource
import shopzen.domain.product.model.Product
import shopzen.domain.product.repository.ProductRepository
import javax.inject.Inject

/**
 * Concrete [ProductRepository] implementation.
 * Delegates to [ProductRemoteDataSource] and maps the DTO to the domain model.
 */
class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource,
) : ProductRepository {

    override suspend fun getProductById(id: Long): Result<Product> {
        return Result.success(remoteDataSource.getProductById(id).product.toDomain())
    }
}
