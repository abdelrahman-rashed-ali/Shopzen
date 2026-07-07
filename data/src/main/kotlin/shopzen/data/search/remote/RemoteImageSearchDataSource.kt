package shopzen.data.search.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import shopzen.data.remote.qualifier.ImageSearchClient
import shopzen.data.search.remote.dto.ImageSearchResponseDto
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class RemoteImageSearchDataSource @Inject constructor(
    @ImageSearchClient private val client: HttpClient
) {
    suspend fun searchByImage(imageBytes: ByteArray): Result<ImageSearchResponseDto> {
        return try {
            val response: HttpResponse = client.submitFormWithBinaryData(
                url = "search/image",
                formData = formData {
                    append("image", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"search_image.jpg\"")
                    })
                }
            )
            
            if (response.status.isSuccess()) {
                val dto: ImageSearchResponseDto = response.body()
                Result.success(dto)
            } else {
                val errorMessage = if (response.status.value == 413) {
                    "Image is too large. Please select a smaller image."
                } else {
                    "Search failed: ${response.status.description}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}
