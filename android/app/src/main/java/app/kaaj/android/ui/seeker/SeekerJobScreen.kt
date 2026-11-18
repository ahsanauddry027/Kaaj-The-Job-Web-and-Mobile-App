package app.kaaj.android.ui.seeker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.SeekerRepository
import app.kaaj.android.data.model.ApplicationStatus
import app.kaaj.android.data.model.Job
import app.kaaj.android.data.model.MyApplication
import app.kaaj.android.data.model.SeekerProfile
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.ui.common.ApplicationStatusPill
import app.kaaj.android.ui.common.Banner
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.FormField
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.label
import app.kaaj.android.ui.jobs.JobDetailContent
import app.kaaj.android.ui.theme.Amber
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

/** One post for a job seeker (wireframe S05) with Save and one-click Apply (M2-2). */
class SeekerJobViewModel(private val seeker: SeekerRepository, private val jobId: String) : KaajViewModel() {
    var job by mutableStateOf<Job?>(null)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var application by mutableStateOf<MyApplication?>(null)
        private set
    var profile by mutableStateOf<SeekerProfile?>(null)
        private set
    var saved by mutableStateOf(false)
        private set
    var applying by mutableStateOf(false)
        private set
    private val idempotencyKey = UUID.randomUUID().toString()

    fun load() {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val post = async { seeker.job(jobId) }
                    val mine = async { seeker.applicationFor(jobId) }
                    val me = async { seeker.profile() }
                    val savedList = async { seeker.savedJobs() }
                    job = post.await()
                    application = mine.await()
                    profile = me.await()
                    saved = savedList.await().any { it.job.id == jobId }
                }
                error = null
            } catch (e: ApiException) {
                if (job == null) error = e else say(e)
            }
        }
    }

    fun toggleSave() {
        viewModelScope.launch {
            try {
                seeker.setSaved(jobId, !saved)
                saved = !saved
                say(if (saved) R.string.job_saved_done else R.string.job_unsaved_done)
            } catch (e: ApiException) {
                say(e)
            }
        }
    }

    fun apply(message: String) {
        applying = true
        viewModelScope.launch {
            try {
                application = seeker.apply(jobId, message, idempotencyKey)
                say(R.string.apply_done)
            } catch (e: ApiException) {
                say(e)
            } finally {
                applying = false
            }
        }
    }
}

@Composable
fun SeekerJobScreen(
    jobId: String,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onOpenApplication: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: SeekerJobViewModel = viewModel { SeekerJobViewModel(container.seeker, jobId) }
    var asking by rememberSaveable { mutableStateOf(false) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            KaajTopBar(title = stringResource(R.string.job_detail_title), onBack = onBack, actions = {
                IconButton(onClick = vm::toggleSave) {
                    Icon(
                        if (vm.saved) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(if (vm.saved) R.string.job_unsave else R.string.job_save),
                        tint = if (vm.saved) Amber else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            })
        },
    ) { padding ->
        val job = vm.job
        val error = vm.error
        when {
            error != null -> ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            job == null -> LoadingBox(Modifier.padding(padding))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ApplyCard(vm, onAsk = { asking = true }, onOpenApplication = onOpenApplication, onOpenProfile = onOpenProfile)
                JobDetailContent(job, showStatus = false)
            }
        }
    }

    if (asking) {
        ApplyDialog(
            onDismiss = { asking = false },
            onSend = { message ->
                asking = false
                vm.apply(message)
            },
        )
    }
}

@Composable
private fun ApplyCard(vm: SeekerJobViewModel, onAsk: () -> Unit, onOpenApplication: (String) -> Unit, onOpenProfile: () -> Unit) {
    val application = vm.application
    SectionCard {
        when {
            application != null && application.status != ApplicationStatus.WITHDRAWN -> {
                Text(stringResource(R.string.apply_already, application.status.label()))
                ApplicationStatusPill(application.status)
                OutlinedButton(onClick = { onOpenApplication(application.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.common_view_details))
                }
            }
            vm.profile?.readyToApply != true -> {
                Banner(
                    title = null,
                    body = stringResource(R.string.apply_need_profile),
                    background = MaterialTheme.colorScheme.tertiaryContainer,
                    content = MaterialTheme.colorScheme.tertiary,
                )
                Button(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.apply_open_profile)) }
            }
            else -> {
                Text(stringResource(R.string.apply_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onAsk, enabled = !vm.applying, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text(stringResource(if (application != null) R.string.apply_again else R.string.apply_button))
                }
            }
        }
    }
}

@Composable
private fun ApplyDialog(onDismiss: () -> Unit, onSend: (String) -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.apply_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.apply_hint), style = MaterialTheme.typography.bodySmall)
                FormField(
                    value = message,
                    onValueChange = { if (it.length <= 500) message = it },
                    label = stringResource(R.string.apply_message),
                    singleLine = false,
                    minLines = 3,
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSend(message) }) { Text(stringResource(R.string.apply_send)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}
