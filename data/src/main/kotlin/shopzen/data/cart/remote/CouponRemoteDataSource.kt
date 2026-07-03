package shopzen.data.cart.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import shopzen.data.remote.qualifier.RestClient
import javax.inject.Inject

@Serializable
data class PriceRulesResponse(val price_rules: List<PriceRuleDto>)

@Serializable
data class PriceRuleDto(
    val id: Long,
    val title: String,
    val target_type: String,
    val value_type: String,
    val value: String,
)

@Serializable
data class DiscountCodesResponse(val discount_codes: List<DiscountCodeDto>)

@Serializable
data class DiscountCodeDto(
    val id: Long,
    val price_rule_id: Long,
    val code: String
)

class CouponRemoteDataSource @Inject constructor(
    @RestClient private val client: HttpClient
) {
    suspend fun getPriceRules(): List<PriceRuleDto> {
        return client.get("price_rules.json").body<PriceRulesResponse>().price_rules
    }

    suspend fun getDiscountCodes(priceRuleId: Long): List<DiscountCodeDto> {
        return client.get("price_rules/$priceRuleId/discount_codes.json").body<DiscountCodesResponse>().discount_codes
    }
}