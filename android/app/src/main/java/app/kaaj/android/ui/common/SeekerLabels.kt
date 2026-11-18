package app.kaaj.android.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.kaaj.android.R
import app.kaaj.android.data.model.Actor
import app.kaaj.android.data.model.ApplicationStatus
import app.kaaj.android.data.model.Reason
import app.kaaj.android.domain.Format
import app.kaaj.android.ui.theme.Amber
import app.kaaj.android.ui.theme.AmberSoft
import app.kaaj.android.ui.theme.Brand
import app.kaaj.android.ui.theme.BrandSoft
import app.kaaj.android.ui.theme.Emerald
import app.kaaj.android.ui.theme.EmeraldSoft
import app.kaaj.android.ui.theme.Red
import app.kaaj.android.ui.theme.RedSoft
import app.kaaj.android.ui.theme.Slate
import app.kaaj.android.ui.theme.SlateSoft
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

// Translated names for the job seeker side (strings_seeker.xml).

@Composable
fun ApplicationStatus.label(): String = stringResource(
    when (this) {
        ApplicationStatus.APPLIED -> R.string.app_status_applied
        ApplicationStatus.VIEWED -> R.string.app_status_viewed
        ApplicationStatus.SHORTLISTED -> R.string.app_status_shortlisted
        ApplicationStatus.INTERVIEW -> R.string.app_status_interview
        ApplicationStatus.HIRED -> R.string.app_status_hired
        ApplicationStatus.DECLINED -> R.string.app_status_declined
        ApplicationStatus.WITHDRAWN -> R.string.app_status_withdrawn
    },
)

/** The employer's button for moving an applicant to this status ("Shortlist", "Hire"…). */
@Composable
fun ApplicationStatus.actionLabel(): String = when (this) {
    ApplicationStatus.SHORTLISTED -> stringResource(R.string.applicant_do_shortlisted)
    ApplicationStatus.INTERVIEW -> stringResource(R.string.applicant_do_interview)
    ApplicationStatus.HIRED -> stringResource(R.string.applicant_do_hired)
    ApplicationStatus.DECLINED -> stringResource(R.string.applicant_do_declined)
    else -> label()
}

fun ApplicationStatus.colors(): Pair<Color, Color> = when (this) {
    ApplicationStatus.APPLIED -> BrandSoft to Brand
    ApplicationStatus.VIEWED -> SlateSoft to Slate
    ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEW -> AmberSoft to Amber
    ApplicationStatus.HIRED -> EmeraldSoft to Emerald
    ApplicationStatus.DECLINED -> RedSoft to Red
    ApplicationStatus.WITHDRAWN -> SlateSoft to Slate
}

@Composable
fun ApplicationStatusPill(status: ApplicationStatus) {
    val (background, content) = status.colors()
    Pill(status.label(), background, content)
}

@Composable
fun Actor.label(): String = stringResource(
    when (this) {
        Actor.SEEKER -> R.string.actor_seeker
        Actor.EMPLOYER -> R.string.actor_employer
        Actor.SYSTEM -> R.string.actor_system
    },
)

/** The seeker's shift choices: the job shifts plus part-time. */
@Composable
fun seekerShiftLabel(code: String): String = when (code) {
    "DAY" -> stringResource(R.string.job_shift_day)
    "NIGHT" -> stringResource(R.string.job_shift_night)
    "ROTATING" -> stringResource(R.string.job_shift_rotating)
    "FLEXIBLE" -> stringResource(R.string.job_shift_flexible)
    "PART_TIME" -> stringResource(R.string.seeker_shift_part_time)
    else -> code
}

@Composable
fun seekerMissingLabel(field: String): String = when (field) {
    "displayName" -> stringResource(R.string.seeker_missing_display_name)
    "occupations" -> stringResource(R.string.seeker_missing_occupations)
    "skills" -> stringResource(R.string.seeker_missing_skills)
    "experience" -> stringResource(R.string.seeker_missing_experience)
    "home" -> stringResource(R.string.seeker_missing_home)
    "expectedWage" -> stringResource(R.string.seeker_missing_expected_wage)
    "shifts" -> stringResource(R.string.seeker_missing_shifts)
    else -> field
}

private fun JsonElement?.text(bangla: Boolean): String = when (this) {
    is JsonPrimitive -> {
        val number = content.toDoubleOrNull()
        when {
            number == null -> content
            number % 1.0 == 0.0 -> Format.localDigits(number.toLong(), bangla)
            else -> Format.km(number, bangla)
        }
    }
    is JsonArray -> joinToString(", ") { it.text(bangla) }
    else -> ""
}

/** "Has 2 of 2 skills asked", "3.2 km from home"… for the codes the backend's Match sends. */
@Composable
fun Reason.text(): String {
    val bangla = isBangla()
    fun value(key: String) = values[key].text(bangla)
    return when (code) {
        "OCCUPATION_MATCH" -> stringResource(R.string.reason_occupation_match)
        "SKILLS_MATCH" -> stringResource(R.string.reason_skills_match, value("matched"), value("asked"))
        "EXPERIENCE_OK" -> stringResource(R.string.reason_experience_ok, value("years"), value("asked"))
        "DOCUMENTS_OK" -> stringResource(R.string.reason_documents_ok)
        "NEAR_HOME" -> stringResource(R.string.reason_near_home, value("km"))
        "REMOTE" -> stringResource(R.string.reason_remote)
        "OTHER_OCCUPATION" -> stringResource(R.string.gap_other_occupation)
        "SKILLS_MISSING" -> stringResource(R.string.gap_skills_missing, value("count"))
        "EXPERIENCE_SHORT" -> stringResource(R.string.gap_experience_short, value("years"), value("asked"))
        "DOCUMENTS_MISSING" -> stringResource(R.string.gap_documents_missing)
        "FAR_FROM_HOME" -> stringResource(R.string.gap_far_from_home, value("km"))
        else -> code
    }
}
