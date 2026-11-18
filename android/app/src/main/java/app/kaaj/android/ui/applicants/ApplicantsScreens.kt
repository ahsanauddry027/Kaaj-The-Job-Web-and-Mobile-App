@file:OptIn(ExperimentalLayoutApi::class)

package app.kaaj.android.ui.applicants

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.ApplicantRepository
import app.kaaj.android.data.model.Applicant
import app.kaaj.android.data.model.ApplicationStatus
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.Format
import app.kaaj.android.ui.common.ApplicationStatusPill
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.FormField
import app.kaaj.android.ui.common.InfoRow
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.actionLabel
import app.kaaj.android.ui.common.documentCodeLabel
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.seekerShiftLabel
import app.kaaj.android.ui.common.text
import app.kaaj.android.ui.common.unit
import app.kaaj.android.ui.seeker.TimelineRow
import app.kaaj.android.ui.theme.Emerald
import app.kaaj.android.ui.theme.Red
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

/** The employer's ranked applicants for one post (M3-2 without AI, wireframe E11). */
class ApplicantsViewModel(private val applicants: ApplicantRepository, private val jobId: String) : KaajViewModel() {
    var items by mutableStateOf<List<Applicant>?>(null)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            try {
                items = applicants.forJob(jobId)
                error = null
            } catch (e: ApiException) {
                if (items == null) error = e else say(e)
            }
        }
    }
}

@Composable
fun ApplicantsScreen(jobId: String, snackbar: SnackbarHostState, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val container = LocalAppContainer.current
    val vm: ApplicantsViewModel = viewModel { ApplicantsViewModel(container.applicants, jobId) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(topBar = { KaajTopBar(title = stringResource(R.string.applicants_title), onBack = onBack) }) { padding ->
        val items = vm.items
        val error = vm.error
        when {
            error != null -> ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            items == null -> LoadingBox(Modifier.padding(padding))
            items.isEmpty() -> Text(
                stringResource(R.string.applicants_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(padding).padding(24.dp),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                item { Text(stringResource(R.string.applicants_ranked), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(items, key = { it.applicationId }) { applicant -> ApplicantCard(applicant, onClick = { onOpen(applicant.applicationId) }) }
            }
        }
    }
}

@Composable
private fun ApplicantCard(applicant: Applicant, onClick: () -> Unit) {
    val bangla = isBangla()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(applicant.seeker?.displayName.orEmpty(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                ApplicationStatusPill(applicant.status)
            }
            ScoreBar(applicant.score)
            applicant.seeker?.let { seeker ->
                Text(
                    listOfNotNull(
                        seeker.areaName,
                        stringResource(R.string.applicant_years, Format.localDigits(seeker.yearsExperience, bangla)),
                        applicant.distanceKm?.let { stringResource(R.string.search_km_away, Format.km(it, bangla)) },
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            applicant.reasons.take(3).forEach { Text("✓ " + it.text(), style = MaterialTheme.typography.bodySmall, color = Emerald) }
            applicant.gaps.take(2).forEach { Text("• " + it.text(), style = MaterialTheme.typography.bodySmall, color = Red) }
        }
    }
}

@Composable
private fun ScoreBar(score: Double) {
    val bangla = isBangla()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(R.string.applicant_match, Format.localDigits((score * 100).toInt(), bangla)),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        LinearProgressIndicator(progress = { score.toFloat() }, modifier = Modifier.fillMaxWidth())
    }
}

/** One applicant (opening marks it "Viewed"), with the profile, reasons, timeline and the decision. */
class ApplicantViewModel(private val applicants: ApplicantRepository, private val id: String) : KaajViewModel() {
    var applicant by mutableStateOf<Applicant?>(null)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var note by mutableStateOf("")
    var interviewPlace by mutableStateOf("")
    /** ISO-8601 instant chosen with the date and time pickers. */
    var interviewAt by mutableStateOf<String?>(null)
    var busy by mutableStateOf(false)
        private set

    fun load() {
        viewModelScope.launch {
            try {
                applicant = applicants.open(id)
                error = null
            } catch (e: ApiException) {
                error = e
            }
        }
    }

    fun decide(status: ApplicationStatus) {
        busy = true
        viewModelScope.launch {
            try {
                applicant = applicants.decide(
                    id, status, note,
                    interviewAt = if (status == ApplicationStatus.INTERVIEW) interviewAt else null,
                    interviewPlace = if (status == ApplicationStatus.INTERVIEW) interviewPlace else null,
                )
                note = ""
                say(R.string.applicant_updated)
            } catch (e: ApiException) {
                say(e)
            } finally {
                busy = false
            }
        }
    }
}

@Composable
fun ApplicantScreen(applicationId: String, snackbar: SnackbarHostState, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: ApplicantViewModel = viewModel { ApplicantViewModel(container.applicants, applicationId) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { if (vm.applicant == null) vm.load() }
    val bangla = isBangla()

    Scaffold(topBar = { KaajTopBar(title = stringResource(R.string.applicant_title), onBack = onBack) }) { padding ->
        val applicant = vm.applicant
        val error = vm.error
        when {
            error != null -> ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            applicant == null -> LoadingBox(Modifier.padding(padding))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val seeker = applicant.seeker
                SectionCard {
                    Text(seeker?.displayName.orEmpty(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    ApplicationStatusPill(applicant.status)
                    ScoreBar(applicant.score)
                    if (seeker != null) {
                        seeker.areaName?.let { InfoRow(stringResource(R.string.seeker_area), it) }
                        InfoRow(stringResource(R.string.seeker_years), Format.localDigits(seeker.yearsExperience, bangla))
                        seeker.education?.let { InfoRow(stringResource(R.string.seeker_education), it) }
                        InfoRow(stringResource(R.string.applicant_phone), seeker.phone ?: stringResource(R.string.applicant_phone_hidden))
                        Format.wage(seeker.expectedWage, null, seeker.wagePeriod?.unit(), bangla)?.let {
                            InfoRow(stringResource(R.string.seeker_expected_wage), it)
                        }
                        if (seeker.occupations.isNotEmpty() || seeker.skills.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (seeker.occupations + seeker.skills).forEach { AssistChip(onClick = {}, label = { Text(Format.termName(it, bangla)) }) }
                            }
                        }
                        if (seeker.documents.isNotEmpty()) {
                            Text(stringResource(R.string.seeker_documents) + ": " + seeker.documents.map { documentCodeLabel(it) }.joinToString(", "))
                        }
                        if (seeker.shifts.isNotEmpty()) {
                            Text(stringResource(R.string.seeker_shifts) + ": " + seeker.shifts.map { seekerShiftLabel(it) }.joinToString(", "))
                        }
                    }
                    applicant.message?.let { InfoRow(stringResource(R.string.applicant_message), it) }
                }
                if (applicant.reasons.isNotEmpty()) {
                    SectionCard(title = stringResource(R.string.applicant_reasons)) {
                        applicant.reasons.forEach { Text("✓ " + it.text(), color = Emerald) }
                    }
                }
                if (applicant.gaps.isNotEmpty()) {
                    SectionCard(title = stringResource(R.string.applicant_gaps)) {
                        applicant.gaps.forEach { Text("• " + it.text(), color = Red) }
                    }
                }
                SectionCard(title = stringResource(R.string.application_timeline)) {
                    applicant.timeline.forEach { TimelineRow(it) }
                }
                if (applicant.nextStatuses.isNotEmpty()) DecisionCard(vm, applicant)
            }
        }
    }
}

@Composable
private fun DecisionCard(vm: ApplicantViewModel, applicant: Applicant) {
    val context = LocalContext.current
    val bangla = isBangla()
    SectionCard(title = stringResource(R.string.applicant_decision)) {
        FormField(
            value = vm.note,
            onValueChange = { if (it.length <= 500) vm.note = it },
            label = stringResource(R.string.applicant_note),
            singleLine = false,
            minLines = 2,
        )
        if (ApplicationStatus.INTERVIEW in applicant.nextStatuses) {
            OutlinedButton(onClick = {
                val now = LocalDateTime.now()
                DatePickerDialog(context, { _, year, month, day ->
                    TimePickerDialog(context, { _, hour, minute ->
                        vm.interviewAt = LocalDateTime.of(year, month + 1, day, hour, minute)
                            .atZone(ZoneId.of("Asia/Dhaka")).toInstant().toString()
                    }, 10, 0, true).show()
                }, now.year, now.monthValue - 1, now.dayOfMonth).show()
            }) {
                Text(vm.interviewAt?.let { Format.dateTime(it, bangla) } ?: stringResource(R.string.applicant_interview_time))
            }
            FormField(
                value = vm.interviewPlace,
                onValueChange = { if (it.length <= 200) vm.interviewPlace = it },
                label = stringResource(R.string.applicant_interview_place),
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            applicant.nextStatuses.forEach { status ->
                if (status == ApplicationStatus.DECLINED) {
                    OutlinedButton(onClick = { vm.decide(status) }, enabled = !vm.busy) {
                        Text(status.actionLabel(), color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Button(
                        onClick = { vm.decide(status) },
                        enabled = !vm.busy,
                        colors = if (status == ApplicationStatus.HIRED) ButtonDefaults.buttonColors(containerColor = Emerald) else ButtonDefaults.buttonColors(),
                    ) { Text(status.actionLabel()) }
                }
            }
        }
    }
}
