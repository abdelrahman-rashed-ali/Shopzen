package shopzen.data.search.repository

import shopzen.data.search.mapper.ImageSearchMapper
import shopzen.data.search.remote.RemoteImageSearchDataSource
import shopzen.domain.catalog.model.Product
import shopzen.domain.search.repository.ImageSearchRepository
import javax.inject.Inject

class ImageSearchRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteImageSearchDataSource
) : ImageSearchRepository {
    override suspend fun searchProductsByImage(imageBytes: ByteArray): Result<List<Product>> {
        val result = remoteDataSource.searchByImage(imageBytes)
        return result.map { responseDto ->
            responseDto.data.map { ImageSearchMapper.mapToDomain(it) }
        }
    }
}
