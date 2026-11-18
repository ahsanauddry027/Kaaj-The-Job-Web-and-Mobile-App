package app.kaaj.android.ui.seeker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.SeekerRepository
import app.kaaj.android.data.model.JobBrief
import app.kaaj.android.data.model.MyApplication
import app.kaaj.android.data.model.TimelineEntry
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.Format
import app.kaaj.android.ui.common.ApplicationStatusPill
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.InfoRow
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.VerifiedBadge
import app.kaaj.android.ui.common.colors
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import app.kaaj.android.ui.common.unit
import kotlinx.coroutines.launch

fun JobBrief.title(bangla: Boolean): String = if (bangla) titleBn.ifBlank { titleEn } else titleEn.ifBlank { titleBn }

/** "My applications" (M2-2, wireframe S07): every application with its status, newest change first. */
class ApplicationsViewModel(private val seeker: SeekerRepository) : KaajViewModel() {
    var items by mutableStateOf<List<MyApplication>?>(null)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            try {
                items = seeker.applications()
                error = null
            } catch (e: ApiException) {
                if (items == null) error = e else say(e)
            }
        }
    }
}

@Composable
fun ApplicationsScreen(snackbar: SnackbarHostState, onOpen: (String) -> Unit) {
    val container = LocalAppContainer.current
    val vm: ApplicationsViewModel = viewModel { ApplicationsViewModel(container.seeker) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(topBar = { KaajTopBar(title = stringResource(R.string.applications_title)) }) { padding ->
        val items = vm.items
        val error = vm.error
        when {
            error != null -> ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            items == null -> LoadingBox(Modifier.padding(padding))
            items.isEmpty() -> Text(
                stringResource(R.string.applications_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(padding).padding(24.dp),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                items(items, key = { it.id }) { application -> ApplicationCard(application, onClick = { onOpen(application.id) }) }
            }
        }
    }
}

@Composable
private fun ApplicationCard(application: MyApplication, onClick: () -> Unit) {
    val bangla = isBangla()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val job = application.job
            Text(job?.title(bangla).orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(job?.employerName.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (job?.employerVerified == true) VerifiedBadge()
            }
            job?.let { brief ->
                Format.wage(brief.wageMin, brief.wageMax, brief.wagePeriod?.unit(), bangla)?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            ApplicationStatusPill(application.status)
            Text(stringResource(R.string.application_applied_on, Format.date(application.appliedAt, bangla)), style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** One application with its timeline, and Withdraw while it is still open. */
class ApplicationViewModel(private val seeker: SeekerRepository, private val id: String) : KaajViewModel() {
    var application by mutableStateOf<MyApplication?>(null)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var busy by mutableStateOf(false)
        private set

    fun load() {
        viewModelScope.launch {
            try {
                application = seeker.application(id)
                error = null
            } catch (e: ApiException) {
                error = e
            }
        }
    }

    fun withdraw() {
        busy = true
        viewModelScope.launch {
            try {
                application = seeker.withdraw(id)
                say(R.string.application_withdrawn_done)
            } catch (e: ApiException) {
                say(e)
            } finally {
                busy = false
            }
        }
    }
}

@Composable
fun ApplicationScreen(applicationId: String, snackbar: SnackbarHostState, onBack: () -> Unit, onOpenJob: (String) -> Unit) {
    val container = LocalAppContainer.current
    val vm: ApplicationViewModel = viewModel { ApplicationViewModel(container.seeker, applicationId) }
    var confirming by remember { mutableStateOf(false) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.load() }
    val bangla = isBangla()

    Scaffold(topBar = { KaajTopBar(title = stringResource(R.string.application_title), onBack = onBack) }) { padding ->
        val application = vm.application
        val error = vm.error
        when {
            error != null -> ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            application == null -> LoadingBox(Modifier.padding(padding))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val job = application.job
                SectionCard {
                    Text(job?.title(bangla).orEmpty(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(job?.employerName.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ApplicationStatusPill(application.status)
                    Text(stringResource(R.string.application_applied_on, Format.date(application.appliedAt, bangla)))
                    application.interviewAt?.let {
                        Text(
                            stringResource(R.string.application_interview, Format.dateTime(it, bangla) + application.interviewPlace?.let { place -> " · $place" }.orEmpty()),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    application.message?.let { InfoRow(stringResource(R.string.apply_message), it) }
                    if (job != null && !job.open) {
                        Text(stringResource(R.string.application_job_closed), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (job != null && job.open) {
                        OutlinedButton(onClick = { onOpenJob(job.id) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.common_view_details)) }
                    }
                }
                SectionCard(title = stringResource(R.string.application_timeline)) {
                    application.timeline.forEach { TimelineRow(it) }
                }
                if (application.canWithdraw) {
                    OutlinedButton(onClick = { confirming = true }, enabled = !vm.busy, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.application_withdraw), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(R.string.common_confirm)) },
            text = { Text(stringResource(R.string.application_withdraw_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    vm.withdraw()
                }) { Text(stringResource(R.string.common_yes)) }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text(stringResource(R.string.common_cancel)) } },
        )
    }
}

/** One step of the timeline: a dot in the status colour, the status, who did it, when, and the note. */
@Composable
fun TimelineRow(entry: TimelineEntry) {
    val bangla = isBangla()
    val (_, color) = entry.toStatus.colors()
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = color, shape = CircleShape, modifier = Modifier.padding(top = 6.dp).size(10.dp)) {}
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(entry.toStatus.label(), fontWeight = FontWeight.SemiBold)
            Text(entry.actor.label() + " · " + Format.date(entry.at, bangla), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            entry.note?.let { Text("“$it”", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
