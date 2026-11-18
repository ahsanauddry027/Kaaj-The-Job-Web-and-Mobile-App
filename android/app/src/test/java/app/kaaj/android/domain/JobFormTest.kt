package app.kaaj.android.domain

import app.kaaj.android.data.model.Job
import app.kaaj.android.data.model.JobStatus
import app.kaaj.android.data.model.Term
import app.kaaj.android.data.model.WagePeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JobFormTest {

    private val driver = Term("occ-1", "ড্রাইভার", "Driver")

    private val complete = JobForm(
        titleBn = "ড্রাইভার",
        descriptionBn = "পিকআপ চালাতে হবে",
        occupation = driver,
        wageMin = "16000",
        wageMax = "20000",
        wagePeriod = WagePeriod.MONTH,
        startTime = "08:00",
        endTime = "17:00",
        latitude = 23.89,
        longitude = 90.40,
        areaName = "Tongi",
    )

    @Test
    fun `a draft needs only a title`() {
        assertEquals(mapOf(JobField.TITLE_BN to FieldError.TITLE_REQUIRED), JobForm().validate(JobFormMode.DRAFT))
        assertTrue(JobForm(titleEn = "Driver").validate(JobFormMode.DRAFT).isEmpty())
    }

    @Test
    fun `publishing needs the description, category, wage, location and hours`() {
        val errors = JobForm(titleBn = "ড্রাইভার").validate(JobFormMode.SUBMIT)
        assertEquals(FieldError.DESCRIPTION_REQUIRED, errors[JobField.DESCRIPTION_BN])
        assertEquals(FieldError.CATEGORY_REQUIRED, errors[JobField.OCCUPATION])
        assertEquals(FieldError.WAGE_REQUIRED, errors[JobField.WAGE_MIN])
        assertEquals(FieldError.WAGE_REQUIRED, errors[JobField.WAGE_MAX])
        assertEquals(FieldError.LOCATION_REQUIRED, errors[JobField.LOCATION])
        assertEquals(FieldError.HOURS_REQUIRED, errors[JobField.START_TIME])
        assertTrue(complete.validate(JobFormMode.SUBMIT).isEmpty())
    }

    @Test
    fun `wage rules`() {
        assertEquals(FieldError.WAGE_RANGE, complete.copy(wageMin = "20000", wageMax = "15000").validate(JobFormMode.SUBMIT)[JobField.WAGE_MAX])
        assertEquals(FieldError.WAGE_POSITIVE, complete.copy(wageMin = "0").validate(JobFormMode.DRAFT)[JobField.WAGE_MIN])
        assertEquals(FieldError.WAGE_POSITIVE, complete.copy(wageMax = "lots").validate(JobFormMode.DRAFT)[JobField.WAGE_MAX])
        assertEquals(FieldError.PERIOD_REQUIRED, complete.copy(wagePeriod = null).validate(JobFormMode.SUBMIT)[JobField.WAGE_PERIOD])
    }

    @Test
    fun `openings and experience ranges`() {
        assertEquals(FieldError.OPENINGS_RANGE, complete.copy(openings = "0").validate(JobFormMode.DRAFT)[JobField.OPENINGS])
        assertEquals(FieldError.OPENINGS_RANGE, complete.copy(openings = "1001").validate(JobFormMode.DRAFT)[JobField.OPENINGS])
        assertEquals(FieldError.EXPERIENCE_RANGE, complete.copy(minExperienceYears = "51").validate(JobFormMode.DRAFT)[JobField.EXPERIENCE])
    }

    @Test
    fun `a remote job needs no map pin`() {
        val remote = complete.copy(remote = true, latitude = null, longitude = null)
        assertTrue(remote.validate(JobFormMode.SUBMIT).isEmpty())
        val input = complete.copy(remote = true).toInput()
        assertNull(input.latitude) // the pin is dropped for remote jobs
        assertTrue(input.remote)
    }

    @Test
    fun `numbers typed with Bangla digits and commas are sent as numbers`() {
        val input = complete.copy(wageMin = "১৬,০০০", wageMax = "২০,০০০", openings = "২").toInput()
        assertEquals(16_000, input.wageMin)
        assertEquals(20_000, input.wageMax)
        assertEquals(2, input.openings)
        assertEquals("08:00-17:00", input.hours)
        assertEquals("occ-1", input.occupationId)
        assertNull(input.titleEn) // blank fields are left out
    }

    @Test
    fun `an existing post fills the form`() {
        val job = Job(
            id = "job-1",
            status = JobStatus.DRAFT,
            titleBn = "লোডার",
            titleEn = "লোডার", // the server copies a missing English title
            hours = "07:00-15:00",
            openings = 3,
            wageMin = 600,
            wageMax = 700,
            wagePeriod = WagePeriod.DAY,
        )
        val form = JobForm.from(job)
        assertEquals("", form.titleEn)
        assertEquals("07:00", form.startTime)
        assertEquals("15:00", form.endTime)
        assertEquals("3", form.openings)
        assertEquals("600", form.wageMin)
    }

    @Test
    fun `server field errors land on the right step`() {
        assertEquals(JobField.START_TIME, JobForm.fieldForServer("hours"))
        assertEquals(JobField.LOCATION, JobForm.fieldForServer("longitude"))
        assertEquals(1, JobForm.stepOf(JobField.START_TIME))
        assertEquals(2, JobForm.stepOf(JobField.LOCATION))
        assertNull(JobForm.fieldForServer("somethingElse"))
    }
}
