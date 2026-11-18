package app.kaaj.android.ui.employer

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.EmployerRepository
import app.kaaj.android.data.PickedFile
import app.kaaj.android.data.model.DocumentType
import app.kaaj.android.data.model.EmployerKind
import app.kaaj.android.data.model.EmployerProfile
import app.kaaj.android.data.model.VerificationStatus
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.FieldError
import app.kaaj.android.domain.Format
import app.kaaj.android.domain.ProfileForm
import app.kaaj.android.ui.common.Banner
import app.kaaj.android.ui.common.ChoiceChips
import app.kaaj.android.ui.common.DocumentPicker
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.FormField
import app.kaaj.android.ui.common.InfoRow
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MapPicker
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.VerificationPill
import app.kaaj.android.ui.common.banner
import app.kaaj.android.ui.common.colors
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import app.kaaj.android.ui.common.missingLabel
import app.kaaj.android.ui.common.text
import kotlinx.coroutines.launch

/** Feature 3: the business profile, the verification document and the review status (wireframe E04–E06). */
class ProfileViewModel(private val employer: EmployerRepository) : KaajViewModel() {
    var loading by mutableStateOf(true)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var profile by mutableStateOf<EmployerProfile?>(null)
        private set
    var form by mutableStateOf(ProfileForm())
        private set
    var errors by mutableStateOf<Map<String, FieldError>>(emptyMap())
        private set
    var serverErrors by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var documentType by mutableStateOf(DocumentType.TRADE_LICENCE)
    var saving by mutableStateOf(false)
        private set
    var uploading by mutableStateOf(false)
        private set
    var resubmitting by mutableStateOf(false)
        private set

    private var loadedOnce = false

    fun load() {
        if (loadedOnce) return
        viewModelScope.launch {
            loading = true
            try {
                show(employer.profile())
                loadedOnce = true
                error = null
            } catch (e: ApiException) {
                error = e
            } finally {
                loading = false
            }
        }
    }

    fun retry() {
        loadedOnce = false
        load()
    }

    private fun show(loaded: EmployerProfile?) {
        profile = loaded
        form = ProfileForm.from(loaded)
        loaded?.documentType?.let { documentType = it }
    }

    /** Called on every keystroke: keeps the value and clears that field's error. */
    fun edit(field: String, change: (ProfileForm) -> ProfileForm) {
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
                show(employer.save(form.toInput()))
                say(R.string.profile_saved)
            } catch (e: ApiException) {
                serverErrors = e.fieldErrors
                say(e)
            } finally {
                saving = false
            }
        }
    }

    fun upload(file: PickedFile) {
        if (profile == null) {
            say(R.string.profile_create_first)
            return
        }
        uploading = true
        viewModelScope.launch {
            try {
                show(employer.uploadDocument(file, documentType))
                say(R.string.upload_uploaded)
            } catch (e: ApiException) {
                say(e)
            } finally {
                uploading = false
            }
        }
    }

    fun resubmit() {
        resubmitting = true
        viewModelScope.launch {
            try {
                show(employer.resubmit())
            } catch (e: ApiException) {
                say(e)
            } finally {
                resubmitting = false
            }
        }
    }
}

@Composable
fun ProfileScreen(snackbar: SnackbarHostState, onSettings: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: ProfileViewModel = viewModel { ProfileViewModel(container.employer) }
    val scope = rememberCoroutineScope()
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            KaajTopBar(title = stringResource(R.string.nav_business), actions = {
                IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings)) }
            })
        },
    ) { padding ->
        val error = vm.error
        when {
            vm.loading -> LoadingBox(Modifier.padding(padding))
            error != null -> ErrorState(error, onRetry = vm::retry, modifier = Modifier.padding(padding))
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                VerificationPanel(vm.profile, resubmitting = vm.resubmitting, onResubmit = vm::resubmit)
                BusinessForm(vm)
                DocumentCard(vm, onProblem = { message -> scope.launch { snackbar.showSnackbar(message) } })
            }
        }
    }
}

/** The "Verification status" card: one banner per state, the reason, what's missing and the dates. */
@Composable
private fun VerificationPanel(profile: EmployerProfile?, resubmitting: Boolean, onResubmit: () -> Unit) {
    val bangla = isBangla()
    val status = profile?.status ?: VerificationStatus.UNVERIFIED
    val (background, content) = status.colors()
    SectionCard(title = stringResource(R.string.verification_title)) {
        Banner(
            title = status.label(),
            body = status.banner(),
            background = background,
            content = content,
            icon = {
                Icon(
                    painterResource(if (status == VerificationStatus.PENDING) R.drawable.ic_pending else R.drawable.ic_verified_user),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            },
        )

        if (status == VerificationStatus.REJECTED && !profile?.verificationNote.isNullOrBlank()) {
            Text(stringResource(R.string.verification_reason), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Text(profile?.verificationNote.orEmpty())
            if (profile?.hasDocument == true) {
                OutlinedButton(onClick = onResubmit, enabled = !resubmitting) { Text(stringResource(R.string.verification_resubmit)) }
            }
        }

        val missing = profile?.missing.orEmpty().filter { it != "document" || status != VerificationStatus.PENDING }
        if (profile != null && missing.isNotEmpty() && status != VerificationStatus.VERIFIED) {
            Text(stringResource(R.string.verification_missing), fontWeight = FontWeight.Bold)
            missing.forEach { Text("• " + missingLabel(it), style = MaterialTheme.typography.bodyMedium) }
        }

        if (profile != null) {
            HorizontalDivider()
            Text(stringResource(R.string.verification_details), fontWeight = FontWeight.Bold)
            InfoRow(
                stringResource(R.string.verification_submitted_at),
                profile.submittedAt?.let { Format.date(it, bangla) } ?: stringResource(R.string.common_not_set),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.verification_title), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.42f))
                Row(Modifier.weight(0.58f)) { VerificationPill(status) }
            }
            if (status == VerificationStatus.VERIFIED && profile.verifiedAt != null) {
                InfoRow(stringResource(R.string.verification_verified_at), Format.date(profile.verifiedAt, bangla))
            }
            if (status == VerificationStatus.VERIFIED && !profile.verificationNote.isNullOrBlank()) {
                InfoRow(stringResource(R.string.verification_note), profile.verificationNote)
            }
            InfoRow(
                stringResource(R.string.verification_document),
                if (profile.hasDocument && profile.documentType != null) {
                    profile.documentType.label() + " · " + profile.documentName.orEmpty()
                } else {
                    stringResource(R.string.verification_no_document)
                },
            )
        }
        if (status == VerificationStatus.VERIFIED) {
            Banner(
                title = null,
                body = stringResource(R.string.verification_change_warning),
                background = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun BusinessForm(vm: ProfileViewModel) {
    val form = vm.form
    @Composable
    fun errorFor(field: String): String? = vm.errors[field]?.text() ?: vm.serverErrors[field]

    SectionCard(title = stringResource(R.string.profile_title)) {
        Text(stringResource(R.string.profile_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        FormField(
            value = form.name,
            onValueChange = { value -> vm.edit("name") { it.copy(name = value) } },
            label = stringResource(R.string.profile_name),
            placeholder = stringResource(R.string.profile_name_placeholder),
            error = errorFor("name"),
        )
        Text(stringResource(R.string.profile_kind), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(
            options = EmployerKind.entries,
            selected = form.kind,
            label = { it.label() },
            onSelect = { kind -> vm.edit("kind") { it.copy(kind = kind) } },
        )
        FormField(
            value = form.address,
            onValueChange = { value -> vm.edit("address") { it.copy(address = value) } },
            label = stringResource(R.string.profile_address),
            placeholder = stringResource(R.string.profile_address_placeholder),
            error = errorFor("address"),
            singleLine = false,
            minLines = 2,
        )
        Text(stringResource(R.string.profile_pin), style = MaterialTheme.typography.labelLarge)
        MapPicker(
            latitude = form.latitude,
            longitude = form.longitude,
            onPick = { lat, lng -> vm.edit("latitude") { it.copy(latitude = lat, longitude = lng) } },
            onClear = { vm.edit("latitude") { it.copy(latitude = null, longitude = null) } },
            error = errorFor("latitude") ?: errorFor("longitude"),
        )
        FormField(
            value = form.contactPerson,
            onValueChange = { value -> vm.edit("contactPerson") { it.copy(contactPerson = value) } },
            label = stringResource(R.string.profile_contact_person),
            error = errorFor("contactPerson"),
        )
        FormField(
            value = form.contactPhone,
            onValueChange = { value -> vm.edit("contactPhone") { it.copy(contactPhone = value) } },
            label = stringResource(R.string.profile_contact_phone),
            supporting = stringResource(R.string.profile_contact_phone_hint),
            error = errorFor("contactPhone"),
            keyboardType = KeyboardType.Phone,
        )
        FormField(
            value = form.contactEmail,
            onValueChange = { value -> vm.edit("contactEmail") { it.copy(contactEmail = value) } },
            label = stringResource(R.string.profile_contact_email),
            error = errorFor("contactEmail"),
            keyboardType = KeyboardType.Email,
        )
        Button(onClick = vm::save, enabled = !vm.saving, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            if (vm.saving) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.profile_save))
            }
        }
    }
}

@Composable
private fun DocumentCard(vm: ProfileViewModel, onProblem: (String) -> Unit) {
    val profile = vm.profile
    SectionCard(title = stringResource(R.string.profile_upload_title)) {
        Text(stringResource(R.string.profile_upload_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (profile == null) {
            Banner(
                title = null,
                body = stringResource(R.string.profile_create_first),
                background = MaterialTheme.colorScheme.surfaceVariant,
                content = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(stringResource(R.string.profile_document_type), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(
            options = DocumentType.entries,
            selected = vm.documentType,
            label = { it.label() },
            onSelect = { vm.documentType = it },
        )
        if (profile?.hasDocument == true) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(R.drawable.ic_description), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(profile.documentName.orEmpty(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
        if (vm.uploading) {
            Text(stringResource(R.string.upload_uploading))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        DocumentPicker(enabled = profile != null && !vm.uploading, onPicked = vm::upload, onProblem = onProblem)
    }
}
