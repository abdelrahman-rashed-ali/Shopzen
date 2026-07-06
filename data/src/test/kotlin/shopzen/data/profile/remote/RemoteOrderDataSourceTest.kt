package shopzen.data.profile.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteOrderDataSourceTest {

    @Test
    fun `getOrderHistory requests customer orders with status any and limit 50`() = runTest {
        var requestedPath = ""
        val engine = MockEngine { request ->
            requestedPath = request.url.fullPath
            respond(
                content = """
                    {
                      "orders": [
                        {
                          "id": 10,
                          "name": "#1010",
                          "total_price": "99.95",
                          "currency": "USD",
                          "financial_status": "paid",
                          "fulfillment_status": "fulfilled",
                          "created_at": "2026-07-01T10:15:30Z",
                          "line_items": []
                        }
                      ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = RemoteOrderDataSource(HttpClient(engine))

        val response = dataSource.getOrderHistory(42L)

        assertTrue(requestedPath.startsWith("/customers/42/orders.json"))
        assertTrue(requestedPath.contains("status=any"))
        assertTrue(requestedPath.contains("limit=50"))
        assertEquals(1, response.orders.size)
        assertEquals("#1010", response.orders.first().name)
    }

    @Test
    fun `getOrderHistory throws on non-success response`() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"error":"bad request"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = RemoteOrderDataSource(HttpClient(engine))

        val result = runCatching { dataSource.getOrderHistory(42L) }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `getOrderDetail requests single order`() = runTest {
        var requestedPath = ""
        val engine = MockEngine { request ->
            requestedPath = request.url.fullPath
            respond(
                content = """
                    {
                      "order": {
                        "id": 10,
                        "name": "#1010",
                        "total_price": "99.95",
                        "currency": "USD",
                        "financial_status": "paid",
                        "fulfillment_status": "fulfilled",
                        "created_at": "2026-07-01T10:15:30Z",
                        "line_items": []
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = RemoteOrderDataSource(HttpClient(engine))

        val response = dataSource.getOrderDetail("10")

        assertEquals("/orders/10.json", requestedPath)
        assertEquals("#1010", response.order.name)
    }

    @Test
    fun `getOrderDetail throws on non-success response`() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"error":"not found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = RemoteOrderDataSource(HttpClient(engine))

        val result = runCatching { dataSource.getOrderDetail("10") }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}
