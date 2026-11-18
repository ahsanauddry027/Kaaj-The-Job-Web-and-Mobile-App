package app.kaaj.android.ui.seeker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.SeekerRepository
import app.kaaj.android.data.TaxonomyRepository
import app.kaaj.android.data.model.SeekerProfile
import app.kaaj.android.data.model.WagePeriod
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.FieldError
import app.kaaj.android.domain.Format
import app.kaaj.android.domain.JobForm
import app.kaaj.android.domain.SeekerForm
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
import app.kaaj.android.ui.common.TermSearch
import app.kaaj.android.ui.common.documentCodeLabel
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import app.kaaj.android.ui.common.seekerMissingLabel
import app.kaaj.android.ui.common.seekerShiftLabel
import app.kaaj.android.ui.common.text
import kotlinx.coroutines.launch

/** The job seeker profile (M1-2, wireframe S02): one profile, no CV, with a completeness meter. */
class SeekerProfileViewModel(private val seeker: SeekerRepository, taxonomy: TaxonomyRepository) : KaajViewModel() {
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var profile by mutableStateOf<SeekerProfile?>(null)
        private set
    var form by mutableStateOf(SeekerForm())
        private set
    var errors by mutableStateOf<Map<String, FieldError>>(emptyMap())
        private set
    var serverErrors by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var saving by mutableStateOf(false)
        private set
    private var loaded = false

    val occupations = TermSearch(viewModelScope) { taxonomy.occupations(it) }
    val skills = TermSearch(viewModelScope) { taxonomy.skills(it) }

    fun load() {
        if (loaded) return
        loading = true
        viewModelScope.launch {
            try {
                val found = seeker.profile()
                profile = found
                form = SeekerForm.from(found)
                loaded = true
                error = null
            } catch (e: ApiException) {
                error = e
            } finally {
                loading = false
            }
        }
    }

    fun retry() {
        loaded = false
        load()
    }

    fun edit(field: String, change: (SeekerForm) -> SeekerForm) {
        form = change(form)
        if (field in errors) errors = errors - field
        if (field in serverErrors) serverErrors = serverErrors - field
    }

    fun save() {
        errors = form.validate()
        serverErrors = emptyMap()
        if (errors.isNotEmpty()) {
            say(R.string.common_fix_errors)
            return
        }
        saving = true
        viewModelScope.launch {
            try {
                val saved = seeker.save(form.toInput())
                profile = saved
                form = SeekerForm.from(saved)
                say(R.string.seeker_saved)
            } catch (e: ApiException) {
                serverErrors = e.fieldErrors
                say(e)
            } finally {
                saving = false
            }
        }
    }
}

@Composable
fun SeekerProfileScreen(snackbar: SnackbarHostState, onSettings: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: SeekerProfileViewModel = viewModel { SeekerProfileViewModel(container.seeker, container.taxonomy) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            KaajTopBar(title = stringResource(R.string.seeker_profile_title), actions = {
                IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings)) }
            })
        },
    ) { padding ->
        val error = vm.error
        when {
            vm.loading -> LoadingBox(Modifier.padding(padding))
            error != null -> ErrorState(error, onRetry = vm::retry, modifier = Modifier.padding(padding))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CompletenessCard(vm.profile)
                SeekerProfileForm(vm)
            }
        }
    }
}

@Composable
private fun CompletenessCard(profile: SeekerProfile?) {
    val bangla = isBangla()
    val percent = profile?.completeness ?: 0
    SectionCard {
        Text(stringResource(R.string.seeker_complete, Format.localDigits(percent, bangla)), fontWeight = FontWeight.Bold)
        LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.seeker_profile_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val missing = profile?.missing ?: listOf("displayName", "occupations")
        if (missing.isNotEmpty()) {
            Text(stringResource(R.string.verification_missing), style = MaterialTheme.typography.labelLarge)
            missing.forEach { Text("• " + seekerMissingLabel(it), style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun SeekerProfileForm(vm: SeekerProfileViewModel) {
    val form = vm.form
    val bangla = isBangla()

    @Composable
    fun errorFor(field: String): String? = vm.errors[field]?.text() ?: vm.serverErrors[field]

    SectionCard {
        FormField(
            value = form.displayName,
            onValueChange = { value -> vm.edit("displayName") { it.copy(displayName = value) } },
            label = stringResource(R.string.seeker_display_name),
            error = errorFor("displayName"),
        )
        FormField(
            value = form.phone,
            onValueChange = { value -> vm.edit("phone") { it.copy(phone = value) } },
            label = stringResource(R.string.seeker_phone),
            supporting = stringResource(R.string.seeker_phone_hint),
            error = errorFor("phone"),
            keyboardType = KeyboardType.Phone,
        )
        MultiTermPicker(
            search = vm.occupations,
            selected = form.occupations,
            label = stringResource(R.string.seeker_occupations),
            placeholder = stringResource(R.string.job_category_placeholder),
            onChange = { list -> vm.edit("occupationIds") { it.copy(occupations = list.take(5)) } },
        )
        if (form.occupations.isEmpty()) {
            Text(stringResource(R.string.v_occupation_needed), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
        errorFor("occupationIds")?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        MultiTermPicker(
            search = vm.skills,
            selected = form.skills,
            label = stringResource(R.string.seeker_skills),
            placeholder = stringResource(R.string.job_skills_placeholder),
            onChange = { list -> vm.edit("skillIds") { it.copy(skills = list.take(20)) } },
        )
        FormField(
            value = form.yearsExperience,
            onValueChange = { value -> vm.edit("yearsExperience") { it.copy(yearsExperience = value) } },
            label = stringResource(R.string.seeker_years),
            keyboardType = KeyboardType.Number,
            error = errorFor("yearsExperience"),
        )
        FormField(
            value = form.education,
            onValueChange = { value -> vm.edit("education") { it.copy(education = value) } },
            label = stringResource(R.string.seeker_education),
            error = errorFor("education"),
        )
        Text(stringResource(R.string.seeker_documents), style = MaterialTheme.typography.labelLarge)
        MultiChoiceChips(
            options = JobForm.DRIVING_CODES,
            selected = form.documents,
            label = { documentCodeLabel(it) },
            onToggle = { code ->
                vm.edit("documents") { it.copy(documents = if (code in it.documents) it.documents - code else it.documents + code) }
            },
        )
        Text(stringResource(R.string.seeker_documents_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    SectionCard(title = stringResource(R.string.seeker_home)) {
        MapPicker(
            latitude = form.latitude,
            longitude = form.longitude,
            onPick = { lat, lng -> vm.edit("homeLatitude") { it.copy(latitude = lat, longitude = lng) } },
            onClear = { vm.edit("homeLatitude") { it.copy(latitude = null, longitude = null) } },
            error = errorFor("homeLatitude"),
        )
        FormField(
            value = form.areaName,
            onValueChange = { value -> vm.edit("homeAreaName") { it.copy(areaName = value) } },
            label = stringResource(R.string.seeker_area),
            placeholder = stringResource(R.string.job_area_placeholder),
            error = errorFor("homeAreaName"),
        )
        Text(stringResource(R.string.seeker_radius, Format.localDigits(form.radiusKm, bangla)), style = MaterialTheme.typography.labelLarge)
        Slider(
            value = form.radiusKm.toFloat(),
            onValueChange = { value -> vm.edit("travelRadiusKm") { it.copy(radiusKm = value.toInt().coerceIn(1, 50)) } },
            valueRange = 1f..50f,
        )
    }

    SectionCard {
        FormField(
            value = form.expectedWage,
            onValueChange = { value -> vm.edit("expectedWage") { it.copy(expectedWage = value) } },
            label = stringResource(R.string.seeker_expected_wage),
            keyboardType = KeyboardType.Number,
            error = errorFor("expectedWage"),
        )
        ChoiceChips(
            options = listOf(WagePeriod.MONTH, WagePeriod.DAY, WagePeriod.WEEK, WagePeriod.HOUR),
            selected = form.wagePeriod,
            label = { it.label() },
            onSelect = { period -> vm.edit("wagePeriod") { it.copy(wagePeriod = period) } },
        )
        errorFor("wagePeriod")?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Text(stringResource(R.string.seeker_shifts), style = MaterialTheme.typography.labelLarge)
        MultiChoiceChips(
            options = SeekerForm.SHIFTS,
            selected = form.shifts,
            label = { seekerShiftLabel(it) },
            onToggle = { code -> vm.edit("shifts") { it.copy(shifts = if (code in it.shifts) it.shifts - code else it.shifts + code) } },
        )
    }

    Button(onClick = vm::save, enabled = !vm.saving, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        if (vm.saving) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(stringResource(R.string.seeker_save))
        }
    }
}
