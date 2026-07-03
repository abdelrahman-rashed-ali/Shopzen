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

enum class AppCurrency(val symbol: String) {
    USD("$"),
    EUR("EUR"),
    GBP("GBP"),
}
