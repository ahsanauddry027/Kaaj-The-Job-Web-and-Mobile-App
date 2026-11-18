@file:OptIn(ExperimentalLayoutApi::class)

package app.kaaj.android.ui.jobs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kaaj.android.R
import app.kaaj.android.data.model.Job
import app.kaaj.android.data.model.JobStatus
import app.kaaj.android.domain.Format
import app.kaaj.android.ui.common.JobStatusPill
import app.kaaj.android.ui.common.VerifiedBadge
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import app.kaaj.android.ui.common.statusReasonLabel
import app.kaaj.android.ui.common.unit

/** The title in the reader's language, falling back to the other one. */
fun Job.title(bangla: Boolean): String =
    if (bangla) titleBn.ifBlank { titleEn } else titleEn.ifBlank { titleBn }

fun Job.description(bangla: Boolean): String? =
    (if (bangla) descriptionBn ?: descriptionEn else descriptionEn ?: descriptionBn)?.takeIf { it.isNotBlank() }

/** "৳16,000 – 20,000 / month", or null when no wage is set yet (drafts). */
@Composable
fun Job.wageText(): String? = Format.wage(wageMin, wageMax, wagePeriod?.unit(), isBangla())

/** "Tongi, Gazipur • Full-time • Experience: 1+ years" */
@Composable
fun Job.summaryLine(): String {
    val bangla = isBangla()
    val place = when {
        remote -> stringResource(R.string.job_remote_label)
        !areaName.isNullOrBlank() -> areaName
        else -> null
    }
    val experience = minExperienceYears?.let {
        if (it == 0) stringResource(R.string.job_no_experience)
        else stringResource(R.string.job_experience, Format.localDigits(it, bangla))
    }
    return listOfNotNull(place, jobType.label(), experience).joinToString(" • ")
}

/** One job post in a list (My jobs, Dashboard). */
@Composable
fun JobCard(job: Job, onClick: () -> Unit, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    val bangla = isBangla()
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        job.title(bangla),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        JobStatusPill(job.status)
                        if (job.employer?.verified == true) VerifiedBadge()
                    }
                }
                if (trailing != null) trailing()
            }
            IconLine(Icons.Filled.LocationOn, job.summaryLine())
            val wage = job.wageText()
            if (wage != null) {
                Text(
                    wage + " • " + stringResource(R.string.job_people, Format.localDigits(job.openings, bangla)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconLine(Icons.Filled.DateRange, job.dateLine(bangla))
            statusReasonLabel(job.statusReason)?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** "Published: 18 November 2026 · Expires: … (30 days left)" or "Created: …". */
@Composable
fun Job.dateLine(bangla: Boolean): String = when {
    status == JobStatus.CLOSED && closedAt != null ->
        stringResource(R.string.job_closed_on, Format.date(closedAt, bangla))
    (status == JobStatus.PUBLISHED || status == JobStatus.PAUSED) && expiresAt != null ->
        stringResource(R.string.job_published_on, Format.date(publishedAt, bangla)) + "\n" +
            stringResource(R.string.job_expires_on, Format.date(expiresAt, bangla), Format.localDigits(Format.daysLeft(expiresAt), bangla))
    else -> stringResource(R.string.job_created_on, Format.date(createdAt, bangla))
}

@Composable
fun IconLine(icon: ImageVector, text: String) {
    if (text.isBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp).padding(top = 1.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
