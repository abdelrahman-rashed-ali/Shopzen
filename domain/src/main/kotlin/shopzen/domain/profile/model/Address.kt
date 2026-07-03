package shopzen.domain.profile.model

data class Address(
    val id: String = "",
    val label: String = "",
    val recipientName: String = "",
    val addressLine1: String = "",
    val addressLine2: String? = null,
    val city: String = "",
    val stateOrProvince: String? = null,
    val postalCode: String = "",
    val country: String = "",
    val phone: String? = null,
    val isDefault: Boolean = false,
)
