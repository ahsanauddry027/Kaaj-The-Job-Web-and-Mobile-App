package app.kaaj.android.ui.jobs

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.EmployerRepository
import app.kaaj.android.data.JobRepository
import app.kaaj.android.data.TaxonomyRepository
import app.kaaj.android.data.model.EmployerProfile
import app.kaaj.android.data.model.Job
import app.kaaj.android.data.model.JobStatus
import app.kaaj.android.data.model.JobType
import app.kaaj.android.data.model.Shift
import app.kaaj.android.data.model.VerificationStatus
import app.kaaj.android.data.model.WagePeriod
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.FieldError
import app.kaaj.android.domain.Format
import app.kaaj.android.domain.JobField
import app.kaaj.android.domain.JobForm
import app.kaaj.android.domain.JobFormMode
import app.kaaj.android.ui.common.Banner
import app.kaaj.android.ui.common.ChoiceChips
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.FormField
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MapPicker
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.MultiChoiceChips
import app.kaaj.android.ui.common.MultiTermPicker
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.SingleTermPicker
import app.kaaj.android.ui.common.TermSearch
import app.kaaj.android.ui.common.documentCodeLabel
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import app.kaaj.android.ui.common.text
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Feature 4: the 4-step "Post a job" wizard (wireframe E07/E08), also used to edit a post.
 * The rules are the backend's JobRules: a draft needs a title; publishing needs the rest.
 */
class JobFormViewModel(
    private val jobs: JobRepository,
    private val employer: EmployerRepository,
    taxonomy: TaxonomyRepository,
    private val jobId: String?,
) : KaajViewModel() {
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var profile by mutableStateOf<EmployerProfile?>(null)
        private set
    var existing by mutableStateOf<Job?>(null)
        private set
    var form by mutableStateOf(JobForm())
        private set
    var step by mutableIntStateOf(0)
        private set
    var errors by mutableStateOf<Map<JobField, FieldError>>(emptyMap())
        private set
    var serverErrors by mutableStateOf<Map<JobField, String>>(emptyMap())
        private set
    var saving by mutableStateOf(false)
        private set
    /** Set when the post was saved: the screen opens its detail page. */
    var savedJobId by mutableStateOf<String?>(null)
        private set

    val occupations = TermSearch(viewModelScope) { taxonomy.occupations(it) }
    val skills = TermSearch(viewModelScope) { taxonomy.skills(it) }

    /** Same key for every press of Save/Publish in this form: the server creates the post only once. */
    private val idempotencyKey = UUID.randomUUID().toString()

    /** Editing a published or paused post always re-publishes it (the backend checks the full rules). */
    val editingLivePost: Boolean get() = existing?.status.let { it == JobStatus.PUBLISHED || it == JobStatus.PAUSED }

    fun load() {
        loading = true
        viewModelScope.launch {
            try {
                profile = employer.profile()
                if (jobId != null && existing == null) {
                    val job = jobs.get(jobId)
                    existing = job
                    form = JobForm.from(job)
                }
                error = null
            } catch (e: ApiException) {
                error = e
            } finally {
                loading = false
            }
        }
    }

    fun edit(field: JobField, change: (JobForm) -> JobForm) {
        form = change(form)
        if (field in errors) errors = errors - field
        if (field in serverErrors) serverErrors = serverErrors - field
        // A fix to one field can clear the error shown on its partner field.
        when (field) {
            JobField.END_TIME -> errors = errors - JobField.START_TIME
            JobField.WAGE_MIN -> errors = errors - JobField.WAGE_MAX
            else -> Unit
        }
    }

    fun next() {
        val stepErrors = form.validate(JobFormMode.SUBMIT).filterKeys { it in JobForm.STEPS[step] }
        errors = stepErrors
        if (stepErrors.isEmpty() && step < JobForm.STEPS.lastIndex) step++
    }

    fun back() {
        if (step > 0) step--
    }

    fun save(submit: Boolean) {
        val mode = if (submit || editingLivePost) JobFormMode.SUBMIT else JobFormMode.DRAFT
        val found = form.validate(mode)
        errors = found
        serverErrors = emptyMap()
        if (found.isNotEmpty()) {
            step = found.keys.minOf { JobForm.stepOf(it) }
            say(R.string.common_fix_errors)
            return
        }
        saving = true
        viewModelScope.launch {
            try {
                val input = form.toInput()
                val current = existing
                val saved = if (current == null) {
                    jobs.create(input, submit, idempotencyKey)
                } else {
                    jobs.update(current.id, input, submit)
                }
                say(
                    when {
                        saved.status == JobStatus.PUBLISHED -> if (current == null) R.string.job_published else R.string.job_updated
                        saved.status == JobStatus.IN_REVIEW -> R.string.job_in_review_saved
                        saved.awaitingVerification -> R.string.job_waiting
                        current == null -> R.string.job_created
                        else -> R.string.job_updated
                    },
                )
                savedJobId = saved.id
            } catch (e: ApiException) {
                val mapped = e.fieldErrors.mapNotNull { (name, message) -> JobForm.fieldForServer(name)?.let { it to message } }.toMap()
                serverErrors = mapped
                if (mapped.isNotEmpty()) step = mapped.keys.minOf { JobForm.stepOf(it) }
                say(e)
            } finally {
                saving = false
            }
        }
    }
}

@Composable
fun JobFormScreen(
    jobId: String?,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: JobFormViewModel = viewModel { JobFormViewModel(container.jobs, container.employer, container.taxonomy, jobId) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { if (vm.profile == null) vm.load() }
    LaunchedEffect(vm.savedJobId) { vm.savedJobId?.let(onSaved) }

    val title = stringResource(if (jobId == null) R.string.job_post_title else R.string.job_edit_title)
    Scaffold(topBar = { KaajTopBar(title = title, onBack = onBack) }) { padding ->
        val error = vm.error
        when {
            vm.loading -> LoadingBox(Modifier.padding(padding))
            error != null -> ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            vm.profile == null -> Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Banner(
                    title = stringResource(R.string.dashboard_no_profile_title),
                    body = stringResource(R.string.job_need_profile),
                    background = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Button(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.job_go_verify)) }
            }
            else -> Column(
                Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (jobId == null) {
                    Text(stringResource(R.string.job_post_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (vm.profile?.status != VerificationStatus.VERIFIED) {
                    Banner(
                        title = stringResource(R.string.job_not_verified_title),
                        body = stringResource(R.string.job_not_verified),
                        background = MaterialTheme.colorScheme.tertiaryContainer,
                        content = MaterialTheme.colorScheme.tertiary,
                    )
                }
                StepHeader(vm.step)
                when (vm.step) {
                    0 -> DetailsStep(vm)
                    1 -> WageStep(vm)
                    2 -> LocationStep(vm)
                    else -> RequirementsStep(vm)
                }
                WizardButtons(vm)
            }
        }
    }
}

@Composable
private fun StepHeader(step: Int) {
    val bangla = isBangla()
    val names = listOf(R.string.job_step_details, R.string.job_step_wage, R.string.job_step_location, R.string.job_step_requirements)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.job_step_of, Format.localDigits(step + 1, bangla), Format.localDigits(names.size, bangla)) +
                " · " + stringResource(names[step]),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        LinearProgressIndicator(progress = { (step + 1) / names.size.toFloat() }, modifier = Modifier.fillMaxWidth())
    }
}

/** The message for a field: the local rule first, otherwise what the server said. */
@Composable
private fun JobFormViewModel.messageFor(field: JobField): String? = errors[field]?.text() ?: serverErrors[field]

@Composable
private fun DetailsStep(vm: JobFormViewModel) {
    val form = vm.form
    SectionCard(title = stringResource(R.string.job_step_details)) {
        FormField(
            value = form.titleBn,
            onValueChange = { value -> vm.edit(JobField.TITLE_BN) { it.copy(titleBn = value) } },
            label = stringResource(R.string.job_title_bn),
            placeholder = stringResource(R.string.job_title_placeholder),
            error = vm.messageFor(JobField.TITLE_BN),
        )
        FormField(
            value = form.titleEn,
            onValueChange = { value -> vm.edit(JobField.TITLE_EN) { it.copy(titleEn = value) } },
            label = stringResource(R.string.job_title_en),
            error = vm.messageFor(JobField.TITLE_EN),
        )
        SingleTermPicker(
            search = vm.occupations,
            selected = form.occupation,
            label = stringResource(R.string.job_category),
            placeholder = stringResource(R.string.job_category_placeholder),
            onSelect = { term -> vm.edit(JobField.OCCUPATION) { it.copy(occupation = term) } },
            error = vm.messageFor(JobField.OCCUPATION),
        )
        FormField(
            value = form.openings,
            onValueChange = { value -> vm.edit(JobField.OPENINGS) { it.copy(openings = value) } },
            label = stringResource(R.string.job_openings),
            keyboardType = KeyboardType.Number,
            error = vm.messageFor(JobField.OPENINGS),
        )
        Text(stringResource(R.string.job_job_type), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(
            options = JobType.entries,
            selected = form.jobType,
            label = { it.label() },
            onSelect = { type -> vm.edit(JobField.JOB_TYPE) { it.copy(jobType = type) } },
        )
        FormField(
            value = form.descriptionBn,
            onValueChange = { value -> vm.edit(JobField.DESCRIPTION_BN) { it.copy(descriptionBn = value) } },
            label = stringResource(R.string.job_description_bn),
            placeholder = stringResource(R.string.job_description_placeholder),
            error = vm.messageFor(JobField.DESCRIPTION_BN),
            singleLine = false,
            minLines = 4,
        )
        FormField(
            value = form.descriptionEn,
            onValueChange = { value -> vm.edit(JobField.DESCRIPTION_EN) { it.copy(descriptionEn = value) } },
            label = stringResource(R.string.job_description_en),
            error = vm.messageFor(JobField.DESCRIPTION_EN),
            singleLine = false,
            minLines = 3,
        )
    }
}

@Composable
private fun WageStep(vm: JobFormViewModel) {
    val form = vm.form
    SectionCard(title = stringResource(R.string.job_step_wage)) {
        Text(stringResource(R.string.job_wage_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FormField(
                value = form.wageMin,
                onValueChange = { value -> vm.edit(JobField.WAGE_MIN) { it.copy(wageMin = value) } },
                label = stringResource(R.string.job_wage_min),
                keyboardType = KeyboardType.Number,
                error = vm.messageFor(JobField.WAGE_MIN),
                modifier = Modifier.weight(1f),
            )
            FormField(
                value = form.wageMax,
                onValueChange = { value -> vm.edit(JobField.WAGE_MAX) { it.copy(wageMax = value) } },
                label = stringResource(R.string.job_wage_max),
                keyboardType = KeyboardType.Number,
                error = vm.messageFor(JobField.WAGE_MAX),
                modifier = Modifier.weight(1f),
            )
        }
        Text(stringResource(R.string.job_wage_period), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(
            options = listOf(WagePeriod.MONTH, WagePeriod.DAY, WagePeriod.WEEK, WagePeriod.HOUR, WagePeriod.PIECE),
            selected = form.wagePeriod,
            label = { it.label() },
            onSelect = { period -> vm.edit(JobField.WAGE_PERIOD) { it.copy(wagePeriod = period) } },
        )
        vm.messageFor(JobField.WAGE_PERIOD)?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        Text(stringResource(R.string.job_shift_label), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(
            options = Shift.entries,
            selected = form.shift,
            label = { it.label() },
            onSelect = { shift -> vm.edit(JobField.SHIFT) { it.copy(shift = shift) } },
        )

        Text(stringResource(R.string.job_hours), style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimeButton(
                label = stringResource(R.string.job_start_time),
                value = form.startTime,
                onPick = { time -> vm.edit(JobField.START_TIME) { it.copy(startTime = time) } },
                modifier = Modifier.weight(1f),
            )
            TimeButton(
                label = stringResource(R.string.job_end_time),
                value = form.endTime,
                onPick = { time -> vm.edit(JobField.END_TIME) { it.copy(endTime = time) } },
                modifier = Modifier.weight(1f),
            )
        }
        vm.messageFor(JobField.START_TIME)?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
}

/** Opens the clock and gives back "HH:MM" (24-hour, as the backend stores it). */
@Composable
private fun TimeButton(label: String, value: String, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bangla = isBangla()
    OutlinedButton(
        modifier = modifier.height(56.dp),
        onClick = {
            val hour = value.substringBefore(':', "9").toIntOrNull() ?: 9
            val minute = value.substringAfter(':', "0").toIntOrNull() ?: 0
            TimePickerDialog(context, { _, h, m -> onPick("%02d:%02d".format(h, m)) }, hour, minute, true).show()
        },
    ) {
        Text(if (value.isBlank()) label else "$label  " + Format.localDigits(value, bangla))
    }
}

@Composable
private fun LocationStep(vm: JobFormViewModel) {
    val form = vm.form
    SectionCard(title = stringResource(R.string.job_step_location)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = form.remote, onCheckedChange = { remote -> vm.edit(JobField.REMOTE) { it.copy(remote = remote) }; vm.edit(JobField.LOCATION) { it } })
            Text(stringResource(R.string.job_remote), modifier = Modifier.weight(1f))
        }
        if (!form.remote) {
            MapPicker(
                latitude = form.latitude,
                longitude = form.longitude,
                onPick = { lat, lng -> vm.edit(JobField.LOCATION) { it.copy(latitude = lat, longitude = lng) } },
                onClear = { vm.edit(JobField.LOCATION) { it.copy(latitude = null, longitude = null) } },
                error = vm.messageFor(JobField.LOCATION),
            )
        }
        FormField(
            value = form.areaName,
            onValueChange = { value -> vm.edit(JobField.AREA_NAME) { it.copy(areaName = value) } },
            label = stringResource(R.string.job_area_name),
            placeholder = stringResource(R.string.job_area_placeholder),
            error = vm.messageFor(JobField.AREA_NAME),
        )
    }
}

@Composable
private fun RequirementsStep(vm: JobFormViewModel) {
    val form = vm.form
    SectionCard(title = stringResource(R.string.job_step_requirements)) {
        MultiTermPicker(
            search = vm.skills,
            selected = form.skills,
            label = stringResource(R.string.job_skills),
            placeholder = stringResource(R.string.job_skills_placeholder),
            onChange = { list -> vm.edit(JobField.SKILLS) { it.copy(skills = list) } },
        )
        Text(stringResource(R.string.job_documents), style = MaterialTheme.typography.labelLarge)
        MultiChoiceChips(
            options = JobForm.DRIVING_CODES,
            selected = form.requiredDocuments,
            label = { documentCodeLabel(it) },
            onToggle = { code ->
                vm.edit(JobField.DOCUMENTS) {
                    it.copy(requiredDocuments = if (code in it.requiredDocuments) it.requiredDocuments - code else it.requiredDocuments + code)
                }
            },
        )
        FormField(
            value = form.minExperienceYears,
            onValueChange = { value -> vm.edit(JobField.EXPERIENCE) { it.copy(minExperienceYears = value) } },
            label = stringResource(R.string.job_min_experience),
            keyboardType = KeyboardType.Number,
            error = vm.messageFor(JobField.EXPERIENCE),
        )
    }
}

@Composable
private fun WizardButtons(vm: JobFormViewModel) {
    val last = vm.step == JobForm.STEPS.lastIndex
    val existing = vm.existing
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (vm.step > 0) {
                OutlinedButton(onClick = vm::back, enabled = !vm.saving, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.common_back)) }
            }
            if (!last) {
                Button(onClick = vm::next, enabled = !vm.saving, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.common_next)) }
            } else {
                Button(onClick = { vm.save(submit = true) }, enabled = !vm.saving, modifier = Modifier.weight(1f)) {
                    if (vm.saving) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(if (existing != null) R.string.job_publish_edit else R.string.job_publish))
                    }
                }
            }
        }
        // A live post can only be saved by re-publishing it; drafts can be saved at any step.
        if (!vm.editingLivePost) {
            TextButton(onClick = { vm.save(submit = false) }, enabled = !vm.saving, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.job_save_draft))
            }
        }
    }
}
