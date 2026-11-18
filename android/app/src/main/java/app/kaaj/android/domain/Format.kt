package app.kaaj.android.domain

import app.kaaj.android.data.model.Term
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max

/** Numbers, money and dates the way Bangladeshi readers expect them (the same rules as web/src/lib/format.ts). */
object Format {

    private const val BANGLA_DIGITS = "০১২৩৪৫৬৭৮৯"
    private val DHAKA: ZoneId = ZoneId.of("Asia/Dhaka")

    /** "১৮০০০" → "18000": people may type Bangla digits in number and phone fields. */
    fun toAsciiDigits(text: String): String = buildString(text.length) {
        for (c in text) {
            val index = BANGLA_DIGITS.indexOf(c)
            append(if (index >= 0) '0' + index else c)
        }
    }

    /** Digits in the reader's language: 2026 → ২০২৬ in Bangla. */
    fun localDigits(text: String, bangla: Boolean): String {
        if (!bangla) return text
        return buildString(text.length) {
            for (c in text) append(if (c in '0'..'9') BANGLA_DIGITS[c - '0'] else c)
        }
    }

    fun localDigits(number: Number, bangla: Boolean): String = localDigits(number.toString(), bangla)

    /** South Asian grouping, as bn-BD and en-BD use: 1234567 → "12,34,567". */
    fun groupDigits(n: Long): String {
        val negative = n < 0
        val digits = (if (negative) -n else n).toString()
        if (digits.length <= 3) return (if (negative) "-" else "") + digits
        val last3 = digits.takeLast(3)
        val rest = digits.dropLast(3)
        val groups = rest.reversed().chunked(2).map { it.reversed() }.reversed()
        return (if (negative) "-" else "") + groups.joinToString(",") + "," + last3
    }

    /** ৳১৮,০০০ in Bangla, ৳18,000 in English. */
    fun taka(amount: Int, bangla: Boolean): String = "৳" + localDigits(groupDigits(amount.toLong()), bangla)

    /** "৳১৬,০০০ – ২০,০০০ / মাস". [periodName] is the translated period ("month"). */
    fun wage(min: Int?, max: Int?, periodName: String?, bangla: Boolean): String? {
        if (min == null && max == null) return null
        val range = if (min != null && max != null && min != max) {
            taka(min, bangla) + " – " + localDigits(groupDigits(max.toLong()), bangla)
        } else {
            taka((min ?: max)!!, bangla)
        }
        return if (periodName.isNullOrBlank()) range else "$range / $periodName"
    }

    /** "18 November 2026" or "১৮ নভেম্বর ২০২৬", in Bangladesh time. Empty for null or bad input. */
    fun date(iso: String?, bangla: Boolean): String {
        if (iso.isNullOrBlank()) return ""
        val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
        val locale = if (bangla) Locale.forLanguageTag("bn-BD") else Locale.ENGLISH
        val text = DateTimeFormatter.ofPattern("d MMMM yyyy", locale).format(instant.atZone(DHAKA))
        return localDigits(text, bangla)
    }

    /** "18 November 2026, 10:30" in Bangladesh time (interviews). */
    fun dateTime(iso: String?, bangla: Boolean): String {
        if (iso.isNullOrBlank()) return ""
        val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
        val locale = if (bangla) Locale.forLanguageTag("bn-BD") else Locale.ENGLISH
        return localDigits(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", locale).format(instant.atZone(DHAKA)), bangla)
    }

    /** Whole days from [now] until [iso], never below 0. */
    fun daysLeft(iso: String?, now: Instant = Instant.now()): Long {
        if (iso.isNullOrBlank()) return 0
        val end = runCatching { Instant.parse(iso) }.getOrNull() ?: return 0
        val millis = end.toEpochMilli() - now.toEpochMilli()
        return max(0L, ceil(millis / 86_400_000.0).toLong())
    }

    fun termName(term: Term?, bangla: Boolean): String = when {
        term == null -> ""
        bangla -> term.nameBn
        else -> term.nameEn
    }

    /** "2.3" km, with one decimal, in the reader's digits. */
    fun km(value: Double, bangla: Boolean): String = localDigits(String.format(Locale.US, "%.1f", value), bangla)

    /** Coordinates shown to people: 23.8103, 90.4125 (4 decimals ≈ 11 m). */
    fun coordinate(value: Double, bangla: Boolean): String = localDigits(String.format(Locale.US, "%.4f", value), bangla)
}
