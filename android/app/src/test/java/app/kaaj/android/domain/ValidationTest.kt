package app.kaaj.android.domain

import app.kaaj.android.data.model.EmployerKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    @Test
    fun `Bangladeshi mobile numbers, the same rule as the server`() {
        assertTrue(Validation.isBdMobile("01712345678"))
        assertTrue(Validation.isBdMobile("+8801712345678"))
        assertTrue(Validation.isBdMobile("+880 1712-345678"))
        assertTrue(Validation.isBdMobile("০১৭১২৩৪৫৬৭৮"))
        assertFalse(Validation.isBdMobile("01212345678")) // no operator starts with 12
        assertFalse(Validation.isBdMobile("0171234567")) // one digit short
        assertFalse(Validation.isBdMobile("phone"))
    }

    @Test
    fun `sign-in needs an email and a password`() {
        assertEquals(
            mapOf("email" to FieldError.REQUIRED, "password" to FieldError.REQUIRED),
            Validation.signIn("", ""),
        )
        assertEquals(mapOf("email" to FieldError.EMAIL), Validation.signIn("karim", "x"))
        assertTrue(Validation.signIn("karim@demo.kaaj", "Kaaj@12345").isEmpty())
    }

    @Test
    fun `registration needs a password of at least 8 characters`() {
        assertEquals(mapOf("password" to FieldError.PASSWORD_LENGTH), Validation.register("new@demo.kaaj", "short"))
        assertTrue(Validation.register("new@demo.kaaj", "long enough").isEmpty())
    }

    @Test
    fun `business profile rules`() {
        val empty = ProfileForm().validate()
        assertEquals(FieldError.NAME_LENGTH, empty["name"])
        assertEquals(FieldError.REQUIRED, empty["address"])
        assertEquals(FieldError.REQUIRED, empty["contactPerson"])
        assertEquals(FieldError.REQUIRED, empty["contactPhone"])
        assertNull(empty["contactEmail"]) // optional

        val good = ProfileForm(
            name = "করিম ট্রান্সপোর্ট",
            kind = EmployerKind.SME,
            address = "Station Road, Tongi",
            contactPerson = "Karim",
            contactPhone = "০১৭১২৩৪৫৬৭৮",
            contactEmail = "",
        )
        assertTrue(good.validate().isEmpty())
        val input = good.toInput()
        assertEquals("01712345678", input.contactPhone) // sent with ASCII digits
        assertNull(input.contactEmail) // blank becomes null

        assertEquals(FieldError.EMAIL, good.copy(contactEmail = "karim@").validate()["contactEmail"])
        assertEquals(FieldError.PHONE, good.copy(contactPhone = "12345").validate()["contactPhone"])
    }

    @Test
    fun `server addresses are cleaned up`() {
        assertEquals("http://10.0.2.2:1027", ServerAddress.normalise("10.0.2.2:1027"))
        assertEquals("http://192.168.0.5:1027", ServerAddress.normalise(" http://192.168.0.5:1027/ "))
        assertEquals("https://kaaj.example.com", ServerAddress.normalise("https://kaaj.example.com"))
        assertNull(ServerAddress.normalise(""))
        assertNull(ServerAddress.normalise("not an address"))
    }
}
