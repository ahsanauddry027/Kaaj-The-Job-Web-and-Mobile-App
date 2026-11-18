@file:OptIn(ExperimentalMaterial3Api::class)

package app.kaaj.android.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.AdminRepository
import app.kaaj.android.data.model.AdminEmployer
import app.kaaj.android.data.model.VerificationStatus
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.Format
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.VerificationPill
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private val QUEUE_TABS = listOf(VerificationStatus.PENDING, VerificationStatus.VERIFIED, VerificationStatus.REJECTED)

/** The admin's list of employers waiting for verification, oldest first (wireframe A04). */
class AdminQueueViewModel(private val admin: AdminRepository) : KaajViewModel() {
    var tab by mutableStateOf(VerificationStatus.PENDING)
        private set
    var items by mutableStateOf<List<AdminEmployer>>(emptyList())
        private set
    var counts by mutableStateOf<Map<VerificationStatus, Long>>(emptyMap())
        private set
    var nextCursor by mutableStateOf<String?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set

    fun refresh(pull: Boolean = false) {
        if (pull) refreshing = true
        viewModelScope.launch {
            try {
                coroutineScope {
                    val page = async { admin.queue(tab, cursor = null) }
                    val totals = async { admin.counts() }
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
    }

    fun select(status: VerificationStatus) {
        if (status == tab) return
        tab = status
        items = emptyList()
        loading = true
        refresh()
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        viewModelScope.launch {
            try {
                val page = admin.queue(tab, cursor)
                items = items + page.items
                nextCursor = page.nextCursor
            } catch (e: ApiException) {
                say(e)
            }
        }
    }
}

@Composable
fun AdminQueueScreen(snackbar: SnackbarHostState, onOpen: (String) -> Unit, onSettings: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: AdminQueueViewModel = viewModel { AdminQueueViewModel(container.admin) }
    val bangla = isBangla()
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        topBar = {
            KaajTopBar(title = stringResource(R.string.admin_title), actions = {
                IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings)) }
            })
        },
    ) { padding ->
        val error = vm.error
        PullToRefreshBox(
            isRefreshing = vm.refreshing,
            onRefresh = { vm.refresh(pull = true) },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Text(stringResource(R.string.admin_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QUEUE_TABS) { status ->
                            FilterChip(
                                selected = vm.tab == status,
                                onClick = { vm.select(status) },
                                label = { Text(tabLabel(status) + " (" + Format.localDigits(vm.counts[status] ?: 0L, bangla) + ")") },
                            )
                        }
                    }
                }
                when {
                    vm.loading -> item { LoadingBox() }
                    error != null -> item { ErrorState(error, onRetry = { vm.refresh() }) }
                    vm.items.isEmpty() -> item {
                        Text(stringResource(R.string.admin_empty), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
                    }
                    else -> {
                        items(vm.items, key = { it.id }) { employer -> EmployerRow(employer, onClick = { onOpen(employer.id) }) }
                        if (vm.nextCursor != null) {
                            item {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    OutlinedButton(onClick = vm::loadMore) { Text(stringResource(R.string.common_load_more)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun tabLabel(status: VerificationStatus): String = stringResource(
    when (status) {
        VerificationStatus.VERIFIED -> R.string.admin_tab_verified
        VerificationStatus.REJECTED -> R.string.admin_tab_rejected
        else -> R.string.admin_tab_pending
    },
)

@Composable
private fun EmployerRow(employer: AdminEmployer, onClick: () -> Unit) {
    val bangla = isBangla()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(employer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(employer.kind.label() + " · " + employer.ownerEmail.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                employer.submittedAt?.let {
                    Text(stringResource(R.string.admin_submitted, Format.date(it, bangla)), style = MaterialTheme.typography.bodySmall)
                }
                VerificationPill(employer.status)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
