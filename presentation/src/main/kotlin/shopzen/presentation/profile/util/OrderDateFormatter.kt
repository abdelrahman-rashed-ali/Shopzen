package shopzen.presentation.profile.util

import java.text.DateFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val orderDatePatterns = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd",
)

internal fun formatOrderDate(createdAt: String, locale: Locale): String? {
    val trimmed = createdAt.trim()
    if (trimmed.isEmpty()) return null

    val parsedDate = orderDatePatterns.firstNotNullOfOrNull { pattern ->
        SimpleDateFormat(pattern, Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }.parseFully(trimmed)
    } ?: return null

    return DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(parsedDate)
}

private fun SimpleDateFormat.parseFully(value: String) =
    ParsePosition(0).let { position ->
        parse(value, position)?.takeIf { position.index == value.length }
    }
