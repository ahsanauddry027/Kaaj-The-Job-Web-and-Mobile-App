@file:OptIn(ExperimentalMaterial3Api::class)

package app.kaaj.android.ui.employer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.EmployerRepository
import app.kaaj.android.data.JobRepository
import app.kaaj.android.data.model.EmployerProfile
import app.kaaj.android.data.model.Job
import app.kaaj.android.data.model.JobStatus
import app.kaaj.android.data.model.VerificationStatus
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.Format
import app.kaaj.android.ui.common.Banner
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.VerificationPill
import app.kaaj.android.ui.common.banner
import app.kaaj.android.ui.common.colors
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import app.kaaj.android.ui.jobs.JobCard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Employer dashboard (wireframe E03): verification state, post counts and the latest posts. */
class DashboardViewModel(
    private val employer: EmployerRepository,
    private val jobs: JobRepository,
) : KaajViewModel() {
    var loading by mutableStateOf(true)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var profile by mutableStateOf<EmployerProfile?>(null)
        private set
    var counts by mutableStateOf<Map<JobStatus, Long>>(emptyMap())
        private set
    var recent by mutableStateOf<List<Job>>(emptyList())
        private set

    fun load(pull: Boolean = false) {
        if (pull) refreshing = true
        viewModelScope.launch {
            try {
                val loaded = employer.profile()
                if (loaded != null) {
                    coroutineScope {
                        val countsCall = async { jobs.counts() }
                        val recentCall = async { jobs.list(status = null, query = null, cursor = null, size = 3).items }
                        counts = countsCall.await()
                        recent = recentCall.await()
                    }
                }
                profile = loaded
                error = null
            } catch (e: ApiException) {
                if (profile == null && recent.isEmpty()) error = e else say(e)
            } finally {
                loading = false
                refreshing = false
            }
        }
    }
}

@Composable
fun DashboardScreen(
    snackbar: SnackbarHostState,
    onOpenProfile: () -> Unit,
    onPostJob: () -> Unit,
    onOpenJob: (String) -> Unit,
    onSeeAll: () -> Unit,
    onSettings: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: DashboardViewModel = viewModel { DashboardViewModel(container.employer, container.jobs) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            KaajTopBar(title = stringResource(R.string.dashboard_title), actions = {
                IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings)) }
            })
        },
    ) { padding ->
        val error = vm.error
        when {
            vm.loading -> LoadingBox(Modifier.padding(padding))
            error != null -> ErrorState(error, onRetry = { vm.load() }, modifier = Modifier.padding(padding))
            else -> PullToRefreshBox(
                isRefreshing = vm.refreshing,
                onRefresh = { vm.load(pull = true) },
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                DashboardContent(vm, onOpenProfile, onPostJob, onOpenJob, onSeeAll)
            }
        }
    }
}

@Composable
private fun DashboardContent(
    vm: DashboardViewModel,
    onOpenProfile: () -> Unit,
    onPostJob: () -> Unit,
    onOpenJob: (String) -> Unit,
    onSeeAll: () -> Unit,
) {
    val profile = vm.profile
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard {
            Text(stringResource(R.string.dashboard_welcome), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (profile != null) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                VerificationPill(profile.status)
            }
            Text(stringResource(R.string.dashboard_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onOpenProfile) { Text(stringResource(R.string.dashboard_view_profile)) }
        }

        if (profile == null) {
            Banner(
                title = stringResource(R.string.dashboard_no_profile_title),
                body = stringResource(R.string.dashboard_no_profile),
                background = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Button(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.job_go_verify)) }
        } else {
            if (profile.status != VerificationStatus.VERIFIED) {
                val (background, content) = profile.status.colors()
                Banner(title = profile.status.label(), body = profile.status.banner(), background = background, content = content)
            }

            StatGrid(vm.counts)

            Button(onClick = onPostJob, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.AddCircle, contentDescription = null)
                Text(stringResource(R.string.nav_post_job), modifier = Modifier.padding(start = 8.dp))
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.dashboard_recent), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onSeeAll) { Text(stringResource(R.string.dashboard_see_all)) }
            }
            if (vm.recent.isEmpty()) {
                Text(stringResource(R.string.job_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            vm.recent.forEach { job -> JobCard(job, onClick = { onOpenJob(job.id) }) }
        }
    }
}

@Composable
private fun StatGrid(counts: Map<JobStatus, Long>) {
    val bangla = isBangla()
    val stats = listOf(
        JobStatus.PUBLISHED to R.string.dashboard_stat_published,
        JobStatus.DRAFT to R.string.dashboard_stat_draft,
        JobStatus.PAUSED to R.string.dashboard_stat_paused,
        JobStatus.CLOSED to R.string.dashboard_stat_closed,
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        stats.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (status, label) ->
                    SectionCard(modifier = Modifier.weight(1f)) {
                        Text(
                            Format.localDigits(counts[status] ?: 0L, bangla),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = status.colors().second,
                        )
                        Text(stringResource(label), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
