package shopzen.data.checkout.remote

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class ExchangeRateRemoteDataSourceTest {

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `getRate returns target rate from successful response`() = runTest {
        val dataSource = ExchangeRateRemoteDataSource(
            client = mockClient(
                status = HttpStatusCode.OK,
                body = """
                    {
                      "result": "success",
                      "base_code": "USD",
                      "rates": { "EGP": 49.25 }
                    }
                """.trimIndent(),
            ),
        )

        val rate = dataSource.getRate("USD", "EGP")

        assertEquals(49.25, rate, 0.0)
    }

    @Test
    fun `getRate returns one when currencies already match`() = runTest {
        val dataSource = ExchangeRateRemoteDataSource(
            client = mockClient(
                status = HttpStatusCode.InternalServerError,
                body = "{}",
            ),
        )

        assertEquals(1.0, dataSource.getRate("EGP", "egp"), 0.0)
    }

    @Test
    fun `getRate fails when response does not include target currency`() {
        val dataSource = ExchangeRateRemoteDataSource(
            client = mockClient(
                status = HttpStatusCode.OK,
                body = """
                    {
                      "result": "success",
                      "base_code": "USD",
                      "rates": { "EUR": 0.9 }
                    }
                """.trimIndent(),
            ),
        )

        val error = assertThrows(IllegalStateException::class.java) {
            runTest { dataSource.getRate("USD", "EGP") }
        }

        assertEquals("Exchange rate missing for USD to EGP", error.message)
    }

    private fun mockClient(
        status: HttpStatusCode,
        body: String,
    ): HttpClient =
        HttpClient(
            MockEngine {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf("Content-Type", "application/json"),
                )
            }
        ) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
}
