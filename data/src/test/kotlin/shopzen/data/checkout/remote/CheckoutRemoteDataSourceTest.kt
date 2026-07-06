package shopzen.data.checkout.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.fullPath
import kotlinx.coroutines.test.runTest
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.After
import org.junit.Test
import kotlinx.serialization.json.buildJsonObject
import io.ktor.serialization.kotlinx.json.json
import shopzen.data.checkout.remote.dto.OrderCreateRestResponse

class CheckoutRemoteDataSourceTest {

    private lateinit var dataSource: CheckoutRemoteDataSource

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `createOrder posts order payload and parses response`() = runTest {
        var requestedPath = ""
        var idempotencyKey = ""
        val engine = MockEngine { request ->
            requestedPath = request.url.fullPath
            idempotencyKey = request.headers["Idempotency-Key"].orEmpty()
            respond(
                content = """{"order":{"id":"order-1","name":"#1001"}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        dataSource = CheckoutRemoteDataSource(
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json()
                }
            }
        )

        val response = dataSource.createOrder(
            body = buildJsonObject { },
            idempotencyKey = IdempotencyKey("abc-123"),
        )

        assertEquals(true, requestedPath.endsWith("orders.json"))
        assertEquals("abc-123", idempotencyKey)
        assertEquals(OrderCreateRestResponse(order = shopzen.data.checkout.remote.dto.OrderDto("order-1", "#1001")), response)
    }

    @Test
    fun `createOrder throws when api returns non-success status`() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"error":"bad request"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        dataSource = CheckoutRemoteDataSource(
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json()
                }
            }
        )

        val result = runCatching {
            dataSource.createOrder(
                body = buildJsonObject { },
                idempotencyKey = IdempotencyKey("abc-123"),
            )
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}
