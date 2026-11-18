@file:OptIn(ExperimentalLayoutApi::class)

package app.kaaj.android.ui.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.JobRepository
import app.kaaj.android.data.model.AllowedAction
import app.kaaj.android.data.model.Job
import app.kaaj.android.data.model.JobStatus
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.Format
import app.kaaj.android.ui.common.Banner
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.InfoRow
import app.kaaj.android.ui.common.JobStatusPill
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.VerifiedBadge
import app.kaaj.android.ui.common.documentCodeLabel
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import app.kaaj.android.ui.common.statusReasonLabel
import kotlinx.coroutines.launch

/** One post with everything about it and the actions allowed now (wireframe E10). */
class JobDetailViewModel(private val jobs: JobRepository, private val jobId: String) : KaajViewModel() {
    var job by mutableStateOf<Job?>(null)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var busy by mutableStateOf(false)
        private set
    var deleted by mutableStateOf(false)
        private set
    var repostedDraft by mutableStateOf<String?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            try {
                job = jobs.get(jobId)
                error = null
            } catch (e: ApiException) {
                if (job == null) error = e else say(e)
            }
        }
    }

    fun act(action: AllowedAction) {
        val current = job ?: return
        busy = true
        viewModelScope.launch {
            try {
                val result = jobs.perform(current, action)
                say(action.doneMessage())
                when (action) {
                    AllowedAction.DELETE -> deleted = true
                    AllowedAction.REPOST -> repostedDraft = result?.id
                    else -> job = result
                }
            } catch (e: ApiException) {
                say(e)
            } finally {
                busy = false
            }
        }
    }
}

@Composable
fun JobDetailScreen(
    jobId: String,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    onReposted: (String) -> Unit,
    onApplicants: (String) -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: JobDetailViewModel = viewModel { JobDetailViewModel(container.jobs, jobId) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(vm.deleted) { if (vm.deleted) onDeleted() }
    LaunchedEffect(vm.repostedDraft) { vm.repostedDraft?.let(onReposted) }

    Scaffold(topBar = { KaajTopBar(title = stringResource(R.string.job_detail_title), onBack = onBack) }) { padding ->
        val job = vm.job
        val error = vm.error
        when {
            error != null -> ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            job == null -> LoadingBox(Modifier.padding(padding))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (job.status != JobStatus.DRAFT) {
                    SectionCard(title = stringResource(R.string.job_applicants)) {
                        Button(onClick = { onApplicants(job.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.applicants_open))
                        }
                    }
                }
                ActionsCard(job, busy = vm.busy, onEdit = { onEdit(job.id) }, onAction = vm::act)
                JobDetailContent(job)
            }
        }
    }
}

/** Everything a job seeker would see about the post. Also used by the admin and seeker screens. */
@Composable
fun JobDetailContent(job: Job, showStatus: Boolean = true) {
    val bangla = isBangla()
    SectionCard {
        Text(job.title(bangla), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (showStatus) JobStatusPill(job.status)
            if (job.employer?.verified == true) VerifiedBadge()
        }
        job.employer?.let { Text(it.name, style = MaterialTheme.typography.titleSmall) }
        if (showStatus) {
            statusReasonLabel(job.statusReason)?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        if (job.repostedFrom != null) {
            Text(stringResource(R.string.job_reposted_from), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    SectionCard {
        job.wageText()?.let { InfoRow(stringResource(R.string.job_salary), it) }
        InfoRow(stringResource(R.string.job_openings), stringResource(R.string.job_people, Format.localDigits(job.openings, bangla)))
        InfoRow(stringResource(R.string.job_job_type), job.jobType.label())
        InfoRow(stringResource(R.string.job_shift_label), job.shift.label())
        job.hours?.let { InfoRow(stringResource(R.string.job_hours), Format.localDigits(it.replace("-", " – "), bangla)) }
        job.occupation?.let { InfoRow(stringResource(R.string.job_category), Format.termName(it, bangla)) }
        InfoRow(
            stringResource(R.string.job_location),
            when {
                job.remote -> stringResource(R.string.job_remote_label)
                !job.areaName.isNullOrBlank() -> job.areaName
                job.latitude != null && job.longitude != null ->
                    Format.coordinate(job.latitude, bangla) + ", " + Format.coordinate(job.longitude, bangla)
                else -> stringResource(R.string.common_not_set)
            },
        )
        InfoRow(
            stringResource(R.string.job_min_experience),
            job.minExperienceYears?.let { if (it == 0) stringResource(R.string.job_no_experience) else Format.localDigits(it, bangla) }
                ?: stringResource(R.string.job_no_experience),
        )
        if (job.status == JobStatus.PUBLISHED || job.status == JobStatus.PAUSED) {
            InfoRow(
                stringResource(R.string.job_expiry),
                Format.date(job.expiresAt, bangla) + " (" +
                    stringResource(R.string.job_days_left, Format.localDigits(Format.daysLeft(job.expiresAt), bangla)) + ")",
            )
        }
    }

    job.description(bangla)?.let { text ->
        SectionCard(title = stringResource(R.string.job_description)) { Text(text) }
    }

    if (job.skills.isNotEmpty() || job.requiredDocuments.isNotEmpty()) {
        SectionCard {
            if (job.skills.isNotEmpty()) {
                Text(stringResource(R.string.job_skills), fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    job.skills.forEach { AssistChip(onClick = {}, label = { Text(Format.termName(it, bangla)) }) }
                }
            }
            if (job.requiredDocuments.isNotEmpty()) {
                Text(stringResource(R.string.job_documents), fontWeight = FontWeight.Bold)
                job.requiredDocuments.forEach { Text("• " + documentCodeLabel(it)) }
            }
        }
    }
}

@Composable
private fun ActionsCard(job: Job, busy: Boolean, onEdit: () -> Unit, onAction: (AllowedAction) -> Unit) {
    var confirming by remember { mutableStateOf<AllowedAction?>(null) }
    SectionCard(title = stringResource(R.string.job_manage)) {
        if (job.awaitingVerification) {
            Banner(
                title = stringResource(R.string.job_not_verified_title),
                body = stringResource(R.string.job_not_verified),
                background = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.tertiary,
            )
        }
        fun handle(action: AllowedAction) {
            when {
                action == AllowedAction.EDIT -> onEdit()
                action.needsConfirmation() -> confirming = action
                else -> onAction(action)
            }
        }
        job.allowedActions.forEach { action ->
            if (action == AllowedAction.SUBMIT || action == AllowedAction.RESUME) {
                Button(onClick = { handle(action) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(action.label()) }
            } else {
                OutlinedButton(onClick = { handle(action) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(action.label()) }
            }
        }
        if (job.status == JobStatus.PUBLISHED || job.status == JobStatus.PAUSED) {
            Text(stringResource(R.string.job_auto_close), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    confirming?.let { action ->
        ConfirmActionDialog(
            action = action,
            onConfirm = {
                confirming = null
                onAction(action)
            },
            onDismiss = { confirming = null },
        )
    }
}
