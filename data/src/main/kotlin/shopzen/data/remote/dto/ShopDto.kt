package shopzen.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopResponseDto(
    @SerialName("shop") val shop: ShopDto
)

@Serializable
data class ShopDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("domain") val domain: String,
    @SerialName("currency") val currency: String,
    @SerialName("country_name") val countryName: String,
    @SerialName("shop_owner") val shopOwner: String,
    @SerialName("plan_name") val planName: String,
    @SerialName("myshopify_domain") val myshopifyDomain: String,
    @SerialName("money_format") val moneyFormat: String,
    @SerialName("iana_timezone") val ianaTimezone: String,
    @SerialName("primary_locale") val primaryLocale: String,
    @SerialName("address1") val address1: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("zip") val zip: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null
)
