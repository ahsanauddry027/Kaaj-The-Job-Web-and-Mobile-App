package app.kaaj.android.domain

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** Why a form field is wrong. The screens turn each one into a translated message (strings.xml `v_*`). */
enum class FieldError {
    REQUIRED,
    TOO_LONG,
    NAME_LENGTH,
    PHONE,
    EMAIL,
    PASSWORD_LENGTH,
    TITLE_REQUIRED,
    TITLE_SHORT,
    DESCRIPTION_REQUIRED,
    CATEGORY_REQUIRED,
    NUMBER,
    OPENINGS_RANGE,
    WAGE_REQUIRED,
    WAGE_POSITIVE,
    WAGE_RANGE,
    PERIOD_REQUIRED,
    LOCATION_REQUIRED,
    HOURS_REQUIRED,
    EXPERIENCE_RANGE,
    YEARS_RANGE,
}

/** The same rules as the server's Bean Validation and the web app's zod schemas. */
object Validation {

    /** Bangladeshi mobile numbers: 01XXXXXXXXX or +8801XXXXXXXXX (spaces and dashes allowed). */
    val BD_MOBILE = Regex("""^(\+?880[-\s]?|0)1[3-9](?:[-\s]?\d){8}$""")
    private val EMAIL = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

    fun isBdMobile(text: String): Boolean = BD_MOBILE.matches(Format.toAsciiDigits(text.trim()))

    fun isEmail(text: String): Boolean = EMAIL.matches(text.trim())

    fun signIn(email: String, password: String): Map<String, FieldError> = buildMap {
        when {
            email.isBlank() -> put("email", FieldError.REQUIRED)
            !isEmail(email) -> put("email", FieldError.EMAIL)
        }
        if (password.isEmpty()) put("password", FieldError.REQUIRED)
    }

    fun register(email: String, password: String): Map<String, FieldError> = buildMap {
        when {
            email.isBlank() -> put("email", FieldError.REQUIRED)
            !isEmail(email) -> put("email", FieldError.EMAIL)
            email.trim().length > 200 -> put("email", FieldError.TOO_LONG)
        }
        when {
            password.length < 8 -> put("password", FieldError.PASSWORD_LENGTH)
            password.length > 100 -> put("password", FieldError.TOO_LONG)
        }
    }
}

/** The server address typed in Settings. */
object ServerAddress {
    /**
     * "10.0.2.2:1027" → "http://10.0.2.2:1027"; "http://192.168.0.5:1027/" → "http://192.168.0.5:1027".
     * Null when it isn't an http(s) address.
     */
    fun normalise(input: String): String? {
        var text = input.trim().trimEnd('/')
        if (text.isEmpty()) return null
        if (!text.startsWith("http://", ignoreCase = true) && !text.startsWith("https://", ignoreCase = true)) {
            text = "http://$text"
        }
        val url = text.toHttpUrlOrNull() ?: return null
        val port = if (url.port == HttpUrl.defaultPort(url.scheme)) "" else ":${url.port}"
        return "${url.scheme}://${url.host}$port"
    }
}
