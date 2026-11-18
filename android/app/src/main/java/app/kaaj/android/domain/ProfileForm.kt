package app.kaaj.android.domain

import app.kaaj.android.data.model.EmployerKind
import app.kaaj.android.data.model.EmployerProfile
import app.kaaj.android.data.model.EmployerProfileInput

/** The "My business" form, as typed. Field names match the API so server errors land on the right field. */
data class ProfileForm(
    val name: String = "",
    val kind: EmployerKind = EmployerKind.SME,
    val address: String = "",
    val contactPerson: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    fun validate(): Map<String, FieldError> = buildMap {
        val trimmedName = name.trim()
        if (trimmedName.length !in 2..120) put("name", FieldError.NAME_LENGTH)
        when {
            address.isBlank() -> put("address", FieldError.REQUIRED)
            address.trim().length > 300 -> put("address", FieldError.TOO_LONG)
        }
        when {
            contactPerson.isBlank() -> put("contactPerson", FieldError.REQUIRED)
            contactPerson.trim().length > 120 -> put("contactPerson", FieldError.TOO_LONG)
        }
        when {
            contactPhone.isBlank() -> put("contactPhone", FieldError.REQUIRED)
            !Validation.isBdMobile(contactPhone) -> put("contactPhone", FieldError.PHONE)
        }
        if (contactEmail.isNotBlank() && !Validation.isEmail(contactEmail)) put("contactEmail", FieldError.EMAIL)
    }

    fun toInput() = EmployerProfileInput(
        name = name.trim(),
        kind = kind,
        address = address.trim(),
        latitude = latitude,
        longitude = longitude,
        contactPerson = contactPerson.trim(),
        contactPhone = Format.toAsciiDigits(contactPhone.trim()),
        contactEmail = contactEmail.trim().ifEmpty { null },
    )

    companion object {
        fun from(profile: EmployerProfile?): ProfileForm = if (profile == null) ProfileForm() else ProfileForm(
            name = profile.name,
            kind = profile.kind,
            address = profile.address.orEmpty(),
            contactPerson = profile.contactPerson.orEmpty(),
            contactPhone = profile.contactPhone.orEmpty(),
            contactEmail = profile.contactEmail.orEmpty(),
            latitude = profile.latitude,
            longitude = profile.longitude,
        )
    }
}
