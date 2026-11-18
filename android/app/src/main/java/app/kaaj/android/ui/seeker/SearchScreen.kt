@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package app.kaaj.android.ui.seeker

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.JobQuery
import app.kaaj.android.data.SeekerRepository
import app.kaaj.android.data.TaxonomyRepository
import app.kaaj.android.data.model.JobHit
import app.kaaj.android.data.model.JobType
import app.kaaj.android.data.model.SearchSort
import app.kaaj.android.data.model.Shift
import app.kaaj.android.data.model.Term
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.Format
import app.kaaj.android.domain.WholeNumber
import app.kaaj.android.ui.common.ChoiceChips
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.FormField
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.SingleTermPicker
import app.kaaj.android.ui.common.TermSearch
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import app.kaaj.android.ui.common.lastKnownLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Job search (M2-1, wireframe S03/S04): keyword, filters, sort, and the saved star on each result. */
class SearchViewModel(private val seeker: SeekerRepository, taxonomy: TaxonomyRepository) : KaajViewModel() {
    var query by mutableStateOf(JobQuery())
        private set
    var category by mutableStateOf<Term?>(null)
        private set
    var wageText by mutableStateOf("")
        private set
    var nearMe by mutableStateOf(false)
        private set
    var hits by mutableStateOf<List<JobHit>>(emptyList())
        private set
    var nextCursor by mutableStateOf<String?>(null)
        private set
    var loading by mutableStateOf(true)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    /** The job seeker's home from their profile (for "near my area"), loaded once. */
    private var home: Pair<Double, Double>? = null
    private var homeChecked = false

    val categories = TermSearch(viewModelScope) { taxonomy.occupations(it) }
    private var pending: Job? = null

    fun refresh(pull: Boolean = false) {
        if (pull) refreshing = true
        run(delayMillis = 0)
    }

    private fun run(delayMillis: Long) {
        pending?.cancel()
        pending = viewModelScope.launch {
            if (delayMillis > 0) delay(delayMillis)
            try {
                if (!homeChecked) {
                    homeChecked = true
                    home = runCatching { seeker.profile() }.getOrNull()?.let { profile ->
                        val lat = profile.homeLatitude
                        val lng = profile.homeLongitude
                        if (lat != null && lng != null) lat to lng else null
                    }
                }
                val page = seeker.search(query, cursor = null)
                hits = page.items
                nextCursor = page.nextCursor
                error = null
            } catch (e: ApiException) {
                if (hits.isEmpty()) error = e else say(e)
            } finally {
                loading = false
                refreshing = false
            }
        }
    }

    fun type(text: String) {
        query = query.copy(text = text)
        run(delayMillis = 400)
    }

    fun setCategory(term: Term?) {
        category = term
        query = query.copy(occupationId = term?.id)
        run(0)
    }

    fun setType(type: JobType?) { query = query.copy(type = type); run(0) }

    fun setShift(shift: Shift?) { query = query.copy(shift = shift); run(0) }

    fun setSort(sort: SearchSort) { query = query.copy(sort = sort); run(0) }

    fun setRadius(km: Double) { query = query.copy(radiusKm = km); run(0) }

    fun setWage(text: String) {
        wageText = text
        query = query.copy(monthlyWageMin = (WholeNumber.parse(text) as? WholeNumber.Value)?.number?.toInt())
        run(500)
    }

    /** True when there is a point to search around (profile home or [phoneLocation]). */
    fun useNearMe(on: Boolean, phoneLocation: Pair<Double, Double>? = null): Boolean {
        val point = phoneLocation ?: home
        if (on && point == null) return false
        nearMe = on
        query = if (on && point != null) {
            query.copy(latitude = point.first, longitude = point.second, radiusKm = query.radiusKm ?: 10.0, sort = SearchSort.NEAREST)
        } else {
            query.copy(latitude = null, longitude = null, sort = if (query.sort == SearchSort.NEAREST) SearchSort.NEWEST else query.sort)
        }
        run(0)
        return true
    }

    val hasHome: Boolean get() = home != null

    fun clearFilters() {
        category = null
        wageText = ""
        nearMe = false
        categories.clear()
        query = JobQuery(text = query.text)
        run(0)
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        viewModelScope.launch {
            try {
                val page = seeker.search(query, cursor)
                hits = hits + page.items
                nextCursor = page.nextCursor
            } catch (e: ApiException) {
                say(e)
            }
        }
    }

    fun toggleSave(hit: JobHit) {
        viewModelScope.launch {
            try {
                seeker.setSaved(hit.job.id, !hit.saved)
                hits = hits.map { if (it.job.id == hit.job.id) it.copy(saved = !hit.saved) else it }
                say(if (hit.saved) R.string.job_unsaved_done else R.string.job_saved_done)
            } catch (e: ApiException) {
                say(e)
            }
        }
    }
}

@Composable
fun SearchScreen(snackbar: SnackbarHostState, onOpenJob: (String) -> Unit, onSettings: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: SearchViewModel = viewModel { SearchViewModel(container.seeker, container.taxonomy) }
    val context = LocalContext.current
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var noLocation by rememberSaveable { mutableStateOf(false) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.refresh() }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        val here = if (granted.values.any { it }) lastKnownLocation(context) else null
        noLocation = !vm.useNearMe(true, here?.let { it.latitude to it.longitude })
    }

    Scaffold(
        topBar = {
            KaajTopBar(title = stringResource(R.string.nav_find_jobs), actions = {
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
                    OutlinedTextField(
                        value = vm.query.text,
                        onValueChange = vm::type,
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.search_near_me), modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            Switch(checked = vm.nearMe, onCheckedChange = { on ->
                                if (!on) {
                                    vm.useNearMe(false)
                                } else if (!vm.useNearMe(true)) {
                                    permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                }
                            })
                        }
                        Text(
                            stringResource(if (noLocation && !vm.nearMe) R.string.search_no_location else R.string.search_near_me_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (noLocation && !vm.nearMe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    Column {
                        TextButton(onClick = { showFilters = !showFilters }) {
                            Icon(if (showFilters) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
                            Text(stringResource(R.string.search_filters))
                        }
                        if (showFilters) Filters(vm)
                    }
                }
                when {
                    vm.loading -> item { LoadingBox() }
                    error != null -> item { ErrorState(error, onRetry = { vm.refresh() }) }
                    vm.hits.isEmpty() -> item {
                        Text(stringResource(R.string.search_empty), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
                    }
                    else -> {
                        items(vm.hits, key = { it.job.id }) { hit ->
                            SeekerJobCard(hit, onClick = { onOpenJob(hit.job.id) }, onToggleSave = { vm.toggleSave(hit) })
                        }
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
private fun Filters(vm: SearchViewModel) {
    val bangla = isBangla()
    SectionCard {
        SingleTermPicker(
            search = vm.categories,
            selected = vm.category,
            label = stringResource(R.string.job_category),
            placeholder = stringResource(R.string.job_category_placeholder),
            onSelect = vm::setCategory,
        )
        if (vm.nearMe) {
            Text(stringResource(R.string.search_radius, Format.localDigits((vm.query.radiusKm ?: 10.0).toInt(), bangla)), style = MaterialTheme.typography.labelLarge)
            ChoiceChips(
                options = listOf(3.0, 5.0, 10.0, 20.0, 50.0),
                selected = vm.query.radiusKm,
                label = { Format.localDigits(it.toInt(), bangla) + " km" },
                onSelect = vm::setRadius,
            )
        }
        Text(stringResource(R.string.job_job_type), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(
            options = listOf<JobType?>(null) + JobType.entries,
            selected = vm.query.type,
            label = { it?.label() ?: stringResource(R.string.search_any_type) },
            onSelect = vm::setType,
        )
        Text(stringResource(R.string.job_shift_label), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(
            options = listOf<Shift?>(null) + Shift.entries,
            selected = vm.query.shift,
            label = { it?.label() ?: stringResource(R.string.search_any_shift) },
            onSelect = vm::setShift,
        )
        FormField(
            value = vm.wageText,
            onValueChange = vm::setWage,
            label = stringResource(R.string.search_wage_min),
            keyboardType = KeyboardType.Number,
        )
        Text(stringResource(R.string.search_sort), style = MaterialTheme.typography.labelLarge)
        ChoiceChips(
            options = if (vm.nearMe) SearchSort.entries else listOf(SearchSort.NEWEST, SearchSort.WAGE),
            selected = vm.query.sort,
            label = {
                stringResource(
                    when (it) {
                        SearchSort.NEWEST -> R.string.search_sort_newest
                        SearchSort.NEAREST -> R.string.search_sort_nearest
                        SearchSort.WAGE -> R.string.search_sort_wage
                    },
                )
            },
            onSelect = vm::setSort,
        )
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            TextButton(onClick = vm::clearFilters) { Text(stringResource(R.string.search_clear)) }
        }
    }
}
