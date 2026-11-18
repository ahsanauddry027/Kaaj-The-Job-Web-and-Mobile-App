@file:OptIn(ExperimentalLayoutApi::class)

package app.kaaj.android.ui.seeker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kaaj.android.R
import app.kaaj.android.data.model.JobHit
import app.kaaj.android.domain.Format
import app.kaaj.android.ui.common.VerifiedBadge
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.jobs.IconLine
import app.kaaj.android.ui.jobs.summaryLine
import app.kaaj.android.ui.jobs.title
import app.kaaj.android.ui.jobs.wageText
import app.kaaj.android.ui.theme.Amber

/** One search result for a job seeker (wireframe S04): title, employer, place, wage, distance and the star. */
@Composable
fun SeekerJobCard(hit: JobHit, onClick: () -> Unit, onToggleSave: () -> Unit, modifier: Modifier = Modifier) {
    val bangla = isBangla()
    val job = hit.job
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 16.dp, end = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(job.title(bangla), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        job.employer?.let { Text(it.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        if (job.employer?.verified == true) VerifiedBadge()
                    }
                }
                IconButton(onClick = onToggleSave) {
                    Icon(
                        if (hit.saved) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(if (hit.saved) R.string.job_unsave else R.string.job_save),
                        tint = if (hit.saved) Amber else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(Modifier.padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                IconLine(Icons.Filled.LocationOn, listOfNotNull(
                    job.summaryLine(),
                    hit.distanceKm?.let { stringResource(R.string.search_km_away, Format.km(it, bangla)) },
                ).joinToString(" • "))
                job.wageText()?.let {
                    Text(
                        it + " • " + stringResource(R.string.job_people, Format.localDigits(job.openings, bangla)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                job.publishedAt?.let { IconLine(Icons.Filled.DateRange, stringResource(R.string.job_published_on, Format.date(it, bangla))) }
            }
        }
    }
}
