package shopzen.data.profile.mapper

import shopzen.domain.profile.model.Address
import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.model.AppTheme
import shopzen.domain.profile.model.UserPreferences
import shopzen.domain.profile.model.UserProfile

/**
 * Stateless mapping functions — Firestore Map<String, Any?> <-> domain models.
 * No class state, no injection.
 */

fun Map<String, Any?>.toUserProfile(): UserProfile = UserProfile(
    uid = this["uid"] as? String ?: "",
    fullName = this["fullName"] as? String ?: "",
    email = this["email"] as? String ?: "",
    phone = this["phone"] as? String ?: "",
    photoUrl = this["photoUrl"] as? String,
)

fun UserProfile.toFirestoreMap(): Map<String, Any?> = mapOf(
    "fullName" to fullName,
    "email" to email,
    "phone" to phone,
    "photoUrl" to photoUrl,
)

fun Map<String, Any?>.toAddress(): Address = Address(
    id = this["id"] as? String ?: "",
    label = this["label"] as? String ?: "",
    recipientName = this["recipientName"] as? String ?: "",
    addressLine1 = this["addressLine1"] as? String ?: "",
    addressLine2 = this["addressLine2"] as? String,
    city = this["city"] as? String ?: "",
    stateOrProvince = this["stateOrProvince"] as? String,
    postalCode = this["postalCode"] as? String ?: "",
    country = this["country"] as? String ?: "",
    phone = this["phone"] as? String,
    isDefault = this["isDefault"] as? Boolean ?: false,
)

fun Address.toFirestoreMap(): Map<String, Any?> = mapOf(
    "label" to label,
    "recipientName" to recipientName,
    "addressLine1" to addressLine1,
    "addressLine2" to addressLine2,
    "city" to city,
    "stateOrProvince" to stateOrProvince,
    "postalCode" to postalCode,
    "country" to country,
    "phone" to phone,
    "isDefault" to isDefault,
)

/** DataStore-side default; DataStore mapping itself lives in LocalPreferencesDataSource. */
fun Map<String, Any?>?.toUserPreferencesOrDefault(): UserPreferences {
    if (this == null) return UserPreferences()
    return UserPreferences(
        language = (this["language"] as? String)
            ?.let { tag -> AppLanguage.values().firstOrNull { it.tag == tag || it.name == tag } }
            ?: AppLanguage.ENGLISH,
        theme = (this["theme"] as? String)
            ?.let { runCatching { AppTheme.valueOf(it.uppercase()) }.getOrNull() }
            ?: AppTheme.SYSTEM,
        currency = (this["currency"] as? String)
            ?.let { runCatching { AppCurrency.valueOf(it.uppercase()) }.getOrNull() }
            ?: AppCurrency.USD,
    )
}

fun UserPreferences.toFirestoreMap(): Map<String, Any?> = mapOf(
    "language" to language.tag,
    "theme" to theme.name.lowercase(),
    "currency" to currency.name,
)
