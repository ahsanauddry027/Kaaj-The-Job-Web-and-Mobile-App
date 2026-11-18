package app.kaaj.android.ui.seeker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.SeekerRepository
import app.kaaj.android.data.model.JobHit
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import kotlinx.coroutines.launch

/** Saved jobs (M2-1): the posts the job seeker starred, while they are still open. */
class SavedViewModel(private val seeker: SeekerRepository) : KaajViewModel() {
    var hits by mutableStateOf<List<JobHit>?>(null)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            try {
                hits = seeker.savedJobs()
                error = null
            } catch (e: ApiException) {
                if (hits == null) error = e else say(e)
            }
        }
    }

    fun remove(hit: JobHit) {
        viewModelScope.launch {
            try {
                seeker.setSaved(hit.job.id, false)
                hits = hits?.filterNot { it.job.id == hit.job.id }
                say(R.string.job_unsaved_done)
            } catch (e: ApiException) {
                say(e)
            }
        }
    }
}

@Composable
fun SavedScreen(snackbar: SnackbarHostState, onOpenJob: (String) -> Unit) {
    val container = LocalAppContainer.current
    val vm: SavedViewModel = viewModel { SavedViewModel(container.seeker) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(topBar = { KaajTopBar(title = stringResource(R.string.nav_saved)) }) { padding ->
        val hits = vm.hits
        val error = vm.error
        when {
            error != null -> ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            hits == null -> LoadingBox(Modifier.padding(padding))
            hits.isEmpty() -> Text(
                stringResource(R.string.saved_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(padding).padding(24.dp),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                items(hits, key = { it.job.id }) { hit ->
                    SeekerJobCard(hit, onClick = { onOpenJob(hit.job.id) }, onToggleSave = { vm.remove(hit) })
                }
            }
        }
    }
}
