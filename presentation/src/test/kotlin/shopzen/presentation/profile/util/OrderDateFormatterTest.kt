package shopzen.presentation.profile.util

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderDateFormatterTest {

    @Test
    fun formats_iso_offset_date() {
        val formatted = formatOrderDate("2026-07-07T14:30:00+02:00", Locale.US)

        assertEquals("Jul 7, 2026", formatted)
    }

    @Test
    fun formats_iso_utc_date() {
        val formatted = formatOrderDate("2026-07-07T12:30:00Z", Locale.US)

        assertEquals("Jul 7, 2026", formatted)
    }

    @Test
    fun returns_null_for_invalid_date() {
        assertNull(formatOrderDate("not-a-date", Locale.US))
    }
}
