package app.kaaj.android.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FormatTest {

    @Test
    fun `Bangla digits become ASCII digits`() {
        assertEquals("01712345678", Format.toAsciiDigits("০১৭১২৩৪৫৬৭৮"))
        assertEquals("18,000 taka", Format.toAsciiDigits("১৮,০০০ taka"))
    }

    @Test
    fun `digits are shown in the reader's language`() {
        assertEquals("২০২৬", Format.localDigits("2026", bangla = true))
        assertEquals("2026", Format.localDigits("2026", bangla = false))
    }

    @Test
    fun `numbers use South Asian grouping`() {
        assertEquals("950", Format.groupDigits(950))
        assertEquals("18,000", Format.groupDigits(18_000))
        assertEquals("1,00,000", Format.groupDigits(100_000))
        assertEquals("12,34,567", Format.groupDigits(1_234_567))
    }

    @Test
    fun `wages read like the web app`() {
        assertEquals("৳16,000 – 20,000 / month", Format.wage(16_000, 20_000, "month", bangla = false))
        assertEquals("৳১৬,০০০ – ২০,০০০ / মাস", Format.wage(16_000, 20_000, "মাস", bangla = true))
        assertEquals("৳800 / day", Format.wage(800, 800, "day", bangla = false))
        assertEquals("৳500", Format.wage(null, 500, null, bangla = false))
        assertNull(Format.wage(null, null, "month", bangla = false))
    }

    @Test
    fun `dates are shown in Bangladesh time`() {
        // 20:00 UTC on 17 November is already 18 November in Dhaka (UTC+6).
        assertEquals("18 November 2026", Format.date("2026-11-17T20:00:00Z", bangla = false))
        assertTrue(Format.date("2026-11-17T20:00:00Z", bangla = true).startsWith("১৮ "))
        assertEquals("", Format.date(null, bangla = false))
        assertEquals("", Format.date("not a date", bangla = false))
    }

    @Test
    fun `days left never goes below zero`() {
        val now = Instant.parse("2026-11-18T00:00:00Z")
        assertEquals(30L, Format.daysLeft("2026-12-18T00:00:00Z", now))
        assertEquals(1L, Format.daysLeft("2026-11-18T01:00:00Z", now))
        assertEquals(0L, Format.daysLeft("2026-11-01T00:00:00Z", now))
    }
}
