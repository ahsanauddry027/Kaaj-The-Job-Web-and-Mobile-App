@file:OptIn(ExperimentalMaterial3Api::class)

package app.kaaj.android.ui.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job as CoroutineJob

/** Feature 4: the employer's posts, filtered by status and searchable (wireframe E09). */
class MyJobsViewModel(private val jobs: JobRepository) : KaajViewModel() {
    var status by mutableStateOf<JobStatus?>(null)
        private set
    var query by mutableStateOf("")
        private set
    var items by mutableStateOf<List<Job>>(emptyList())
        private set
    var counts by mutableStateOf<Map<JobStatus, Long>>(emptyMap())
        private set
    var nextCursor by mutableStateOf<String?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var busyId by mutableStateOf<String?>(null)
        private set
    /** Set after "Repost": the screen opens the new draft in the form. */
    var openDraft by mutableStateOf<String?>(null)

    private var pending: CoroutineJob? = null

    fun refresh(pull: Boolean = false) {
        if (pull) refreshing = true
        pending?.cancel()
        pending = viewModelScope.launch { loadFirstPage() }
    }

    private suspend fun loadFirstPage() {
        try {
            coroutineScope {
                val page = async { jobs.list(status, query, cursor = null) }
                val totals = async { jobs.counts() }
                val first = page.await()
                items = first.items
                nextCursor = first.nextCursor
                counts = totals.await()
            }
            error = null
        } catch (e: ApiException) {
            if (items.isEmpty()) error = e else say(e)
        } finally {
            loading = false
            refreshing = false
        }
    }

    fun filter(newStatus: JobStatus?) {
        status = newStatus
        loading = true
        refresh()
    }

    /** Waits until the user stops typing for a moment, then searches. */
    fun search(text: String) {
        query = text
        pending?.cancel()
        pending = viewModelScope.launch {
            delay(350)
            loadFirstPage()
        }
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        if (loadingMore) return
        loadingMore = true
        viewModelScope.launch {
            try {
                val page = jobs.list(status, query, cursor)
                items = items + page.items
                nextCursor = page.nextCursor
            } catch (e: ApiException) {
                say(e)
            } finally {
                loadingMore = false
            }
        }
    }

    fun act(job: Job, action: AllowedAction) {
        busyId = job.id
        viewModelScope.launch {
            try {
                val result = jobs.perform(job, action)
                say(action.doneMessage())
                if (action == AllowedAction.REPOST && result != null) openDraft = result.id
                loadFirstPage()
            } catch (e: ApiException) {
                say(e)
            } finally {
                busyId = null
            }
        }
    }
}

@Composable
fun MyJobsScreen(
    snackbar: SnackbarHostState,
    onOpenJob: (String) -> Unit,
    onEditJob: (String) -> Unit,
    onPostJob: () -> Unit,
    onSettings: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: MyJobsViewModel = viewModel { MyJobsViewModel(container.jobs) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(vm.openDraft) {
        vm.openDraft?.let {
            vm.openDraft = null
            onEditJob(it)
        }
    }

    Scaffold(
        topBar = {
            KaajTopBar(title = stringResource(R.string.nav_my_jobs), actions = {
                IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings)) }
            })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPostJob,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.nav_post_job)) },
            )
        },
    ) { padding ->
        val error = vm.error
        PullToRefreshBox(
            isRefreshing = vm.refreshing,
            onRefresh = { vm.refresh(pull = true) },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item { StatusFilter(vm.status, vm.counts, onSelect = vm::filter) }
                item {
                    OutlinedTextField(
                        value = vm.query,
                        onValueChange = vm::search,
                        placeholder = { Text(stringResource(R.string.job_search_placeholder)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                when {
                    vm.loading -> item { LoadingBox() }
                    error != null -> item { ErrorState(error, onRetry = { vm.refresh() }) }
                    vm.items.isEmpty() -> item {
                        Text(
                            stringResource(R.string.job_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    }
                    else -> {
                        items(vm.items, key = { it.id }) { job ->
                            JobCard(
                                job = job,
                                onClick = { onOpenJob(job.id) },
                                trailing = {
                                    if (vm.busyId == job.id) {
                                        CircularProgressIndicator(Modifier.padding(12.dp))
                                    } else {
                                        JobActionsMenu(job, enabled = vm.busyId == null) { action ->
                                            if (action == AllowedAction.EDIT) onEditJob(job.id) else vm.act(job, action)
                                        }
                                    }
                                },
                            )
                        }
                        if (vm.nextCursor != null) {
                            item {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    OutlinedButton(onClick = vm::loadMore, enabled = !vm.loadingMore) {
                                        Text(stringResource(R.string.common_load_more))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** "All (5) · Draft (1) · In review (0) · Published (3) …" */
@Composable
private fun StatusFilter(selected: JobStatus?, counts: Map<JobStatus, Long>, onSelect: (JobStatus?) -> Unit) {
    val bangla = isBangla()
    val total = counts.values.sum()
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.job_all) + " (" + Format.localDigits(total, bangla) + ")") },
            )
        }
        items(JobStatus.entries.toList()) { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelect(status) },
                label = { Text(status.label() + " (" + Format.localDigits(counts[status] ?: 0L, bangla) + ")") },
            )
        }
    }
}
