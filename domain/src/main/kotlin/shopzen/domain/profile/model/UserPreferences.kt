package shopzen.domain.profile.model

data class UserPreferences(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val theme: AppTheme = AppTheme.SYSTEM,
    val currency: AppCurrency = AppCurrency.USD,
)

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    ARABIC("ar"),
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class AppCurrency(
    val symbol: String,
    /** Exchange rate relative to 1 USD. */
    val rateFromUsd: Double,
) {
    USD("$", 1.0),
    EUR("€", 0.92),
    GBP("£", 0.79),
    EGP("EGP", 49.0),
    SAR("SAR", 3.75),
    AED("AED", 3.67),
}
