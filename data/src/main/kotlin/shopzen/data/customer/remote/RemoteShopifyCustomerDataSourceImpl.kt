package shopzen.data.customer.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import shopzen.data.customer.remote.dto.CreateCustomerRequestDto
import shopzen.data.customer.remote.dto.CreateCustomerResponseDto
import shopzen.data.customer.remote.dto.CustomerRequestBodyDto
import shopzen.data.customer.remote.dto.CustomerSearchResponseDto
import shopzen.data.customer.remote.dto.CustomerDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ktor-backed implementation of [RemoteShopifyCustomerDataSource].
 *
 * The [HttpClient] is pre-configured by [shopzen.app.di.network.NetworkModule]
 * with the correct Shopify base URL, API version, and `X-Shopify-Access-Token`
 * header — exactly as [shopzen.data.catalog.remote.RemoteCatalogDataSource] uses it.
 *
 * Only the relative path (`customers.json`) is needed here.
 */
@Singleton
class RemoteShopifyCustomerDataSourceImpl @Inject constructor(
    @shopzen.data.remote.qualifier.RestClient private val client: HttpClient,
) : RemoteShopifyCustomerDataSource {

    override suspend fun createCustomer(
        email: String,
        firstName: String,
        lastName: String,
    ): Long {
        val response: CreateCustomerResponseDto = client.post("customers.json") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateCustomerRequestDto(
                    customer = CustomerRequestBodyDto(
                        firstName = firstName,
                        lastName  = lastName,
                        email     = email,
                    )
                )
            )
        }.body()

        return response.customer.id
    }

    override suspend fun getCustomerByEmail(email: String): CustomerDto? {
        val response: CustomerSearchResponseDto = client.get("customers/search.json") {
            url {
                parameters.append("query", "email:$email")
            }
        }.body()
        
        return response.customers.firstOrNull()
    }
}
