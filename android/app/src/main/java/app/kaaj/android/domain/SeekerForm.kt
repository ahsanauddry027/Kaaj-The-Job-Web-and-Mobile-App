package app.kaaj.android.domain

import app.kaaj.android.data.model.SeekerProfile
import app.kaaj.android.data.model.SeekerProfileInput
import app.kaaj.android.data.model.Term
import app.kaaj.android.data.model.WagePeriod

/** The job seeker's profile form (M1-2), as typed. Field names match the API for server errors. */
data class SeekerForm(
    val displayName: String = "",
    val phone: String = "",
    val occupations: List<Term> = emptyList(),
    val skills: List<Term> = emptyList(),
    val yearsExperience: String = "",
    val education: String = "",
    val documents: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val areaName: String = "",
    val radiusKm: Int = 10,
    val expectedWage: String = "",
    val wagePeriod: WagePeriod? = WagePeriod.MONTH,
    val shifts: List<String> = emptyList(),
) {
    fun validate(): Map<String, FieldError> = buildMap {
        val name = displayName.trim()
        when {
            name.length < 2 -> put("displayName", FieldError.REQUIRED)
            name.length > 80 -> put("displayName", FieldError.TOO_LONG)
        }
        if (phone.isNotBlank() && !Validation.isBdMobile(phone)) put("phone", FieldError.PHONE)
        val years = WholeNumber.parse(yearsExperience)
        if (years is WholeNumber.Invalid || (years is WholeNumber.Value && years.number > 60)) {
            put("yearsExperience", FieldError.YEARS_RANGE)
        }
        if (education.trim().length > 200) put("education", FieldError.TOO_LONG)
        if (areaName.trim().length > 120) put("homeAreaName", FieldError.TOO_LONG)
        val wage = WholeNumber.parse(expectedWage)
        when {
            wage is WholeNumber.Invalid || (wage is WholeNumber.Value && wage.number <= 0) -> put("expectedWage", FieldError.WAGE_POSITIVE)
            wage is WholeNumber.Value && wagePeriod == null -> put("wagePeriod", FieldError.PERIOD_REQUIRED)
        }
    }

    /** Applying needs a name and at least one occupation, the same rule as the backend. */
    val readyToApply: Boolean get() = displayName.trim().length >= 2 && occupations.isNotEmpty()

    fun toInput(): SeekerProfileInput {
        val wage = (WholeNumber.parse(expectedWage) as? WholeNumber.Value)?.number?.toInt()
        val hasHome = latitude != null && longitude != null
        return SeekerProfileInput(
            displayName = displayName.trim(),
            phone = Format.toAsciiDigits(phone.trim()).ifEmpty { null },
            occupationIds = occupations.map { it.id },
            skillIds = skills.map { it.id },
            yearsExperience = (WholeNumber.parse(yearsExperience) as? WholeNumber.Value)?.number?.toInt() ?: 0,
            education = education.trim().ifEmpty { null },
            documents = documents,
            homeLatitude = if (hasHome) latitude else null,
            homeLongitude = if (hasHome) longitude else null,
            homeAreaName = areaName.trim().ifEmpty { null },
            travelRadiusKm = radiusKm.toDouble(),
            expectedWage = wage,
            wagePeriod = if (wage == null) null else wagePeriod,
            shifts = shifts,
        )
    }

    companion object {
        /** The shifts a job seeker can choose (the job post's four, plus part-time). */
        val SHIFTS = listOf("DAY", "NIGHT", "ROTATING", "PART_TIME", "FLEXIBLE")

        fun from(profile: SeekerProfile?): SeekerForm = if (profile == null) SeekerForm() else SeekerForm(
            displayName = profile.displayName,
            phone = profile.phone.orEmpty(),
            occupations = profile.occupations,
            skills = profile.skills,
            yearsExperience = if (profile.yearsExperience == 0) "" else profile.yearsExperience.toString(),
            education = profile.education.orEmpty(),
            documents = profile.documents,
            latitude = profile.homeLatitude,
            longitude = profile.homeLongitude,
            areaName = profile.homeAreaName.orEmpty(),
            radiusKm = profile.travelRadiusKm.toInt().coerceIn(1, 100),
            expectedWage = profile.expectedWage?.toString().orEmpty(),
            wagePeriod = profile.wagePeriod ?: WagePeriod.MONTH,
            shifts = profile.shifts,
        )
    }
}
