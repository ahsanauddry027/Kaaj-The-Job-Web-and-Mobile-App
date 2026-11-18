package app.kaaj.android.domain

import app.kaaj.android.data.model.Term
import app.kaaj.android.data.model.WagePeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeekerFormTest {

    private val driver = Term("occ-1", "ড্রাইভার", "Driver")

    @Test
    fun `a name is enough to save, an occupation is needed to apply`() {
        val form = SeekerForm(displayName = "রহিম")
        assertTrue(form.validate().isEmpty())
        assertFalse(form.readyToApply)
        assertTrue(form.copy(occupations = listOf(driver)).readyToApply)
        assertEquals(FieldError.REQUIRED, SeekerForm(displayName = " ").validate()["displayName"])
    }

    @Test
    fun `phone, years and wage rules`() {
        val form = SeekerForm(displayName = "Rahim")
        assertEquals(FieldError.PHONE, form.copy(phone = "12345").validate()["phone"])
        assertTrue(form.copy(phone = "০১৮১১২২৩৩৪৪").validate().isEmpty())
        assertEquals(FieldError.YEARS_RANGE, form.copy(yearsExperience = "61").validate()["yearsExperience"])
        assertEquals(FieldError.WAGE_POSITIVE, form.copy(expectedWage = "0").validate()["expectedWage"])
        assertEquals(FieldError.PERIOD_REQUIRED, form.copy(expectedWage = "800", wagePeriod = null).validate()["wagePeriod"])
    }

    @Test
    fun `the input uses ASCII digits and leaves out empty fields`() {
        val input = SeekerForm(
            displayName = " রহিম ",
            phone = "০১৮১১২২৩৩৪৪",
            occupations = listOf(driver),
            yearsExperience = "৫",
            expectedWage = "১৮,০০০",
            wagePeriod = WagePeriod.MONTH,
            latitude = 23.89,
            longitude = null,
        ).toInput()
        assertEquals("রহিম", input.displayName)
        assertEquals("01811223344", input.phone)
        assertEquals(listOf("occ-1"), input.occupationIds)
        assertEquals(5, input.yearsExperience)
        assertEquals(18_000, input.expectedWage)
        assertNull(input.education)
        assertNull(input.homeLatitude) // half a point is dropped
        assertNull(input.homeLongitude)

        val noWage = SeekerForm(displayName = "Rahim", wagePeriod = WagePeriod.DAY).toInput()
        assertNull(noWage.expectedWage)
        assertNull(noWage.wagePeriod) // no amount, no period
    }
}
