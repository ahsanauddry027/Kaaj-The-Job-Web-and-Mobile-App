package app.kaaj.android.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kaaj.android.R
import app.kaaj.android.data.model.AllowedAction
import app.kaaj.android.data.model.DocumentType
import app.kaaj.android.data.model.EmployerKind
import app.kaaj.android.data.model.JobStatus
import app.kaaj.android.data.model.JobType
import app.kaaj.android.data.model.Role
import app.kaaj.android.data.model.Shift
import app.kaaj.android.data.model.VerificationStatus
import app.kaaj.android.data.model.WagePeriod
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

// Translated names for the API's enum values (strings.xml / values-bn/strings.xml).

@Composable
fun Role.label(): String = stringResource(
    when (this) {
        Role.SEEKER -> R.string.role_seeker
        Role.EMPLOYER -> R.string.role_employer
        Role.ADMIN -> R.string.role_admin
    },
)

@Composable
fun EmployerKind.label(): String = stringResource(
    when (this) {
        EmployerKind.HOUSEHOLD -> R.string.kind_household
        EmployerKind.SME -> R.string.kind_sme
        EmployerKind.COMPANY -> R.string.kind_company
        EmployerKind.AGENCY -> R.string.kind_agency
        EmployerKind.NGO -> R.string.kind_ngo
    },
)

@Composable
fun DocumentType.label(): String = stringResource(
    when (this) {
        DocumentType.TRADE_LICENCE -> R.string.doc_type_trade_licence
        DocumentType.NATIONAL_ID -> R.string.doc_type_national_id
    },
)

@Composable
fun VerificationStatus.label(): String = stringResource(
    when (this) {
        VerificationStatus.UNVERIFIED -> R.string.verification_status_unverified
        VerificationStatus.PENDING -> R.string.verification_status_pending
        VerificationStatus.VERIFIED -> R.string.verification_status_verified
        VerificationStatus.REJECTED -> R.string.verification_status_rejected
    },
)

@Composable
fun VerificationStatus.banner(): String = stringResource(
    when (this) {
        VerificationStatus.UNVERIFIED -> R.string.verification_banner_unverified
        VerificationStatus.PENDING -> R.string.verification_banner_pending
        VerificationStatus.VERIFIED -> R.string.verification_banner_verified
        VerificationStatus.REJECTED -> R.string.verification_banner_rejected
    },
)

/** Background and text colour for a verification status. */
fun VerificationStatus.colors(): Pair<Color, Color> = when (this) {
    VerificationStatus.UNVERIFIED -> SlateSoft to Slate
    VerificationStatus.PENDING -> AmberSoft to Amber
    VerificationStatus.VERIFIED -> EmeraldSoft to Emerald
    VerificationStatus.REJECTED -> RedSoft to Red
}

@Composable
fun JobStatus.label(): String = stringResource(
    when (this) {
        JobStatus.DRAFT -> R.string.job_status_draft
        JobStatus.IN_REVIEW -> R.string.job_status_in_review
        JobStatus.PUBLISHED -> R.string.job_status_published
        JobStatus.PAUSED -> R.string.job_status_paused
        JobStatus.CLOSED -> R.string.job_status_closed
        JobStatus.BLOCKED -> R.string.job_status_blocked
    },
)

fun JobStatus.colors(): Pair<Color, Color> = when (this) {
    JobStatus.DRAFT -> SlateSoft to Slate
    JobStatus.IN_REVIEW -> AmberSoft to Amber
    JobStatus.PUBLISHED -> EmeraldSoft to Emerald
    JobStatus.PAUSED -> BrandSoft to Brand
    JobStatus.CLOSED -> SlateSoft to Slate
    JobStatus.BLOCKED -> RedSoft to Red
}

/** Why a post is in its state ("Closed automatically after 30 days"), or null for no extra note. */
@Composable
fun statusReasonLabel(reason: String?): String? = when (reason) {
    "EXPIRED" -> stringResource(R.string.job_reason_expired)
    "CLOSED_BY_EMPLOYER" -> stringResource(R.string.job_reason_closed_by_employer)
    "AWAITING_VERIFICATION" -> stringResource(R.string.job_reason_awaiting_verification)
    "SCREENING_REVIEW" -> stringResource(R.string.job_reason_screening_review)
    "SCREENING_BLOCKED" -> stringResource(R.string.job_reason_screening_blocked)
    "REPORTED" -> stringResource(R.string.job_reason_reported)
    "REMOVED" -> stringResource(R.string.job_reason_removed)
    null, "" -> null
    else -> reason
}

@Composable
fun JobType.label(): String = stringResource(
    when (this) {
        JobType.FULL_TIME -> R.string.job_type_full_time
        JobType.PART_TIME -> R.string.job_type_part_time
        JobType.DAILY -> R.string.job_type_daily
    },
)

@Composable
fun Shift.label(): String = stringResource(
    when (this) {
        Shift.DAY -> R.string.job_shift_day
        Shift.NIGHT -> R.string.job_shift_night
        Shift.ROTATING -> R.string.job_shift_rotating
        Shift.FLEXIBLE -> R.string.job_shift_flexible
    },
)

/** "month" — used after a wage: "৳18,000 / month". */
@Composable
fun WagePeriod.unit(): String = stringResource(
    when (this) {
        WagePeriod.HOUR -> R.string.job_period_hour
        WagePeriod.DAY -> R.string.job_period_day
        WagePeriod.WEEK -> R.string.job_period_week
        WagePeriod.MONTH -> R.string.job_period_month
        WagePeriod.PIECE -> R.string.job_period_piece
    },
)

/** "Per month" — the choice in the form. */
@Composable
fun WagePeriod.label(): String = stringResource(
    when (this) {
        WagePeriod.HOUR -> R.string.job_period_label_hour
        WagePeriod.DAY -> R.string.job_period_label_day
        WagePeriod.WEEK -> R.string.job_period_label_week
        WagePeriod.MONTH -> R.string.job_period_label_month
        WagePeriod.PIECE -> R.string.job_period_label_piece
    },
)

@Composable
fun AllowedAction.label(): String = stringResource(
    when (this) {
        AllowedAction.EDIT -> R.string.job_action_edit
        AllowedAction.SUBMIT -> R.string.job_action_submit
        AllowedAction.PAUSE -> R.string.job_action_pause
        AllowedAction.RESUME -> R.string.job_action_resume
        AllowedAction.CLOSE -> R.string.job_action_close
        AllowedAction.REPOST -> R.string.job_action_repost
        AllowedAction.DELETE -> R.string.job_action_delete
    },
)

/** Names for required-document codes: driving licences, or "Certificate" for CERT:<uuid>. */
@Composable
fun documentCodeLabel(code: String): String = when (code) {
    "DRIVING_MOTORCYCLE" -> stringResource(R.string.doc_code_driving_motorcycle)
    "DRIVING_LIGHT" -> stringResource(R.string.doc_code_driving_light)
    "DRIVING_PRO_LIGHT" -> stringResource(R.string.doc_code_driving_pro_light)
    "DRIVING_PRO_MEDIUM" -> stringResource(R.string.doc_code_driving_pro_medium)
    "DRIVING_PRO_HEAVY" -> stringResource(R.string.doc_code_driving_pro_heavy)
    else -> if (code.startsWith("CERT:")) stringResource(R.string.doc_code_certificate) else code
}

/** "What's still missing" items from the profile API. */
@Composable
fun missingLabel(field: String): String = when (field) {
    "name" -> stringResource(R.string.profile_missing_name)
    "kind" -> stringResource(R.string.profile_missing_kind)
    "address" -> stringResource(R.string.profile_missing_address)
    "contactPerson" -> stringResource(R.string.profile_missing_contact_person)
    "contactPhone" -> stringResource(R.string.profile_missing_contact_phone)
    "document" -> stringResource(R.string.profile_missing_document)
    else -> field
}

@Composable
fun JobStatusPill(status: JobStatus, modifier: Modifier = Modifier) {
    val (background, content) = status.colors()
    Pill(status.label(), background, content, modifier)
}

@Composable
fun VerificationPill(status: VerificationStatus, modifier: Modifier = Modifier) {
    val (background, content) = status.colors()
    Pill(status.label(), background, content, modifier)
}

/** The green "Verified" badge every post of a verified employer carries. */
@Composable
fun VerifiedBadge(modifier: Modifier = Modifier) {
    Pill(
        text = stringResource(R.string.verification_badge),
        background = Emerald,
        content = Color.White,
        modifier = modifier,
        icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) },
    )
}
