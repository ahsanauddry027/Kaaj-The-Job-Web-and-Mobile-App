package app.kaaj.android.domain

import app.kaaj.android.data.model.Job
import app.kaaj.android.data.model.JobInput
import app.kaaj.android.data.model.JobType
import app.kaaj.android.data.model.Shift
import app.kaaj.android.data.model.Term
import app.kaaj.android.data.model.WagePeriod

enum class JobField {
    TITLE_BN, TITLE_EN, OCCUPATION, OPENINGS, JOB_TYPE, DESCRIPTION_BN, DESCRIPTION_EN,
    WAGE_MIN, WAGE_MAX, WAGE_PERIOD, SHIFT, START_TIME, END_TIME,
    LOCATION, AREA_NAME, REMOTE,
    SKILLS, DOCUMENTS, EXPERIENCE,
}

/** "Save as draft" checks only the basics; "Publish" checks everything the server needs to publish. */
enum class JobFormMode { DRAFT, SUBMIT }

/** A whole number typed by a person: empty, a value, or something that isn't a whole number. */
sealed interface WholeNumber {
    data object Empty : WholeNumber
    data class Value(val number: Long) : WholeNumber
    data object Invalid : WholeNumber

    companion object {
        /** Bangla digits, spaces and thousands commas are accepted: "১৮,০০০" → 18000. */
        fun parse(text: String): WholeNumber {
            val clean = Format.toAsciiDigits(text).replace(",", "").filterNot { it.isWhitespace() }
            if (clean.isEmpty()) return Empty
            if (!clean.all { it in '0'..'9' } || clean.length > 9) return Invalid
            return Value(clean.toLong())
        }
    }
}

private fun WholeNumber.valueOrNull(): Long? = (this as? WholeNumber.Value)?.number

/**
 * The "Post a job" wizard's values, kept as typed text (Bangla digits allowed) and converted when sent.
 * The rules match the backend's JobRules and the web app's validateJobForm.
 */
data class JobForm(
    val titleBn: String = "",
    val titleEn: String = "",
    val descriptionBn: String = "",
    val descriptionEn: String = "",
    val occupation: Term? = null,
    val openings: String = "1",
    val jobType: JobType = JobType.FULL_TIME,
    val wageMin: String = "",
    val wageMax: String = "",
    val wagePeriod: WagePeriod? = WagePeriod.MONTH,
    val shift: Shift = Shift.DAY,
    /** "08:00" */
    val startTime: String = "",
    val endTime: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val areaName: String = "",
    val remote: Boolean = false,
    val skills: List<Term> = emptyList(),
    val requiredDocuments: List<String> = emptyList(),
    val minExperienceYears: String = "",
) {

    fun validate(mode: JobFormMode): Map<JobField, FieldError> {
        val errors = linkedMapOf<JobField, FieldError>()
        val title = titleBn.trim().ifEmpty { titleEn.trim() }
        when {
            title.isEmpty() -> errors[JobField.TITLE_BN] = FieldError.TITLE_REQUIRED
            title.length < 2 -> errors[JobField.TITLE_BN] = FieldError.TITLE_SHORT
            title.length > 120 -> errors[JobField.TITLE_BN] = FieldError.TOO_LONG
        }

        val openingsNumber = WholeNumber.parse(openings)
        if (openingsNumber is WholeNumber.Invalid ||
            (openingsNumber is WholeNumber.Value && openingsNumber.number !in 1L..1000L)
        ) {
            errors[JobField.OPENINGS] = FieldError.OPENINGS_RANGE
        }

        val min = WholeNumber.parse(wageMin)
        val max = WholeNumber.parse(wageMax)
        if (min is WholeNumber.Invalid || (min is WholeNumber.Value && min.number <= 0)) errors[JobField.WAGE_MIN] = FieldError.WAGE_POSITIVE
        if (max is WholeNumber.Invalid || (max is WholeNumber.Value && max.number <= 0)) errors[JobField.WAGE_MAX] = FieldError.WAGE_POSITIVE
        val minValue = min.valueOrNull()
        val maxValue = max.valueOrNull()
        if (minValue != null && maxValue != null && JobField.WAGE_MIN !in errors && JobField.WAGE_MAX !in errors && minValue > maxValue) {
            errors[JobField.WAGE_MAX] = FieldError.WAGE_RANGE
        }

        val years = WholeNumber.parse(minExperienceYears)
        if (years is WholeNumber.Invalid || (years is WholeNumber.Value && years.number > 50)) {
            errors[JobField.EXPERIENCE] = FieldError.EXPERIENCE_RANGE
        }

        if (mode == JobFormMode.SUBMIT) {
            if (descriptionBn.isBlank() && descriptionEn.isBlank()) errors[JobField.DESCRIPTION_BN] = FieldError.DESCRIPTION_REQUIRED
            if (occupation == null) errors[JobField.OCCUPATION] = FieldError.CATEGORY_REQUIRED
            if (min is WholeNumber.Empty) errors.putIfAbsent(JobField.WAGE_MIN, FieldError.WAGE_REQUIRED)
            if (max is WholeNumber.Empty) errors.putIfAbsent(JobField.WAGE_MAX, FieldError.WAGE_REQUIRED)
            if (wagePeriod == null) errors[JobField.WAGE_PERIOD] = FieldError.PERIOD_REQUIRED
            if (!remote && (latitude == null || longitude == null)) errors[JobField.LOCATION] = FieldError.LOCATION_REQUIRED
            if (startTime.isBlank() || endTime.isBlank()) errors[JobField.START_TIME] = FieldError.HOURS_REQUIRED
        }
        return errors
    }

    fun toInput(): JobInput = JobInput(
        titleBn = titleBn.trim().ifEmpty { null },
        titleEn = titleEn.trim().ifEmpty { null },
        descriptionBn = descriptionBn.trim().ifEmpty { null },
        descriptionEn = descriptionEn.trim().ifEmpty { null },
        occupationId = occupation?.id,
        jobType = jobType,
        shift = shift,
        hours = if (startTime.isNotBlank() && endTime.isNotBlank()) "$startTime-$endTime" else null,
        openings = WholeNumber.parse(openings).valueOrNull()?.toInt(),
        wageMin = WholeNumber.parse(wageMin).valueOrNull()?.toInt(),
        wageMax = WholeNumber.parse(wageMax).valueOrNull()?.toInt(),
        wagePeriod = wagePeriod,
        latitude = if (remote) null else latitude,
        longitude = if (remote) null else longitude,
        areaName = areaName.trim().ifEmpty { null },
        remote = remote,
        minExperienceYears = WholeNumber.parse(minExperienceYears).valueOrNull()?.toInt(),
        skillIds = skills.map { it.id },
        requiredDocuments = requiredDocuments,
    )

    companion object {
        /** The fields on each wizard step, in order. */
        val STEPS: List<Set<JobField>> = listOf(
            setOf(JobField.TITLE_BN, JobField.TITLE_EN, JobField.OCCUPATION, JobField.OPENINGS, JobField.JOB_TYPE, JobField.DESCRIPTION_BN, JobField.DESCRIPTION_EN),
            setOf(JobField.WAGE_MIN, JobField.WAGE_MAX, JobField.WAGE_PERIOD, JobField.SHIFT, JobField.START_TIME, JobField.END_TIME),
            setOf(JobField.LOCATION, JobField.AREA_NAME, JobField.REMOTE),
            setOf(JobField.SKILLS, JobField.DOCUMENTS, JobField.EXPERIENCE),
        )

        fun stepOf(field: JobField): Int = STEPS.indexOfFirst { field in it }.coerceAtLeast(0)

        /** Server field names (JobInput) → form fields, for errors that come back from the API. */
        fun fieldForServer(name: String): JobField? = when (name) {
            "titleBn" -> JobField.TITLE_BN
            "titleEn" -> JobField.TITLE_EN
            "descriptionBn" -> JobField.DESCRIPTION_BN
            "descriptionEn" -> JobField.DESCRIPTION_EN
            "occupationId" -> JobField.OCCUPATION
            "openings" -> JobField.OPENINGS
            "jobType" -> JobField.JOB_TYPE
            "wageMin" -> JobField.WAGE_MIN
            "wageMax" -> JobField.WAGE_MAX
            "wagePeriod" -> JobField.WAGE_PERIOD
            "shift" -> JobField.SHIFT
            "hours" -> JobField.START_TIME
            "latitude", "longitude" -> JobField.LOCATION
            "areaName" -> JobField.AREA_NAME
            "remote" -> JobField.REMOTE
            "skillIds" -> JobField.SKILLS
            "requiredDocuments" -> JobField.DOCUMENTS
            "minExperienceYears" -> JobField.EXPERIENCE
            else -> null
        }

        /** Fills the form from a saved post (for editing). The server copies a missing title to both languages. */
        fun from(job: Job): JobForm {
            val parts = job.hours?.split("-").orEmpty()
            return JobForm(
                titleBn = job.titleBn,
                titleEn = if (job.titleEn == job.titleBn) "" else job.titleEn,
                descriptionBn = job.descriptionBn.orEmpty(),
                descriptionEn = if (job.descriptionEn == job.descriptionBn) "" else job.descriptionEn.orEmpty(),
                occupation = job.occupation,
                openings = job.openings.toString(),
                jobType = job.jobType,
                wageMin = job.wageMin?.toString().orEmpty(),
                wageMax = job.wageMax?.toString().orEmpty(),
                wagePeriod = job.wagePeriod,
                shift = job.shift,
                startTime = parts.getOrNull(0).orEmpty(),
                endTime = parts.getOrNull(1).orEmpty(),
                latitude = job.latitude,
                longitude = job.longitude,
                areaName = job.areaName.orEmpty(),
                remote = job.remote,
                skills = job.skills,
                requiredDocuments = job.requiredDocuments,
                minExperienceYears = job.minExperienceYears?.toString().orEmpty(),
            )
        }

        /** The driving licence codes an employer can require (the backend's DocumentCode). */
        val DRIVING_CODES = listOf(
            "DRIVING_MOTORCYCLE",
            "DRIVING_LIGHT",
            "DRIVING_PRO_LIGHT",
            "DRIVING_PRO_MEDIUM",
            "DRIVING_PRO_HEAVY",
        )
    }
}
