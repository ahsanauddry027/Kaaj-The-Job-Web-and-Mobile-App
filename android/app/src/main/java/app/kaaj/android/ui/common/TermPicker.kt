@file:OptIn(ExperimentalLayoutApi::class)

package app.kaaj.android.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kaaj.android.R
import app.kaaj.android.data.model.Term
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.Format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Search-as-you-type over the taxonomy (occupations or skills). The backend matches Bangla,
 * Banglish and English ("driver", "ড্রাইভার", "chalok") with PostgreSQL trigram similarity.
 */
class TermSearch(private val scope: CoroutineScope, private val search: suspend (String) -> List<Term>) {
    var query by mutableStateOf("")
        private set
    var results by mutableStateOf<List<Term>>(emptyList())
        private set
    private var pending: Job? = null

    fun type(text: String) {
        query = text
        pending?.cancel()
        if (text.isBlank()) {
            results = emptyList()
            return
        }
        pending = scope.launch {
            delay(250)
            results = try {
                search(text)
            } catch (e: ApiException) {
                emptyList()
            }
        }
    }

    fun clear() {
        pending?.cancel()
        query = ""
        results = emptyList()
    }
}

/** Pick one term (the job's category). */
@Composable
fun SingleTermPicker(
    search: TermSearch,
    selected: Term?,
    label: String,
    placeholder: String,
    onSelect: (Term?) -> Unit,
    error: String? = null,
) {
    val bangla = isBangla()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (selected != null) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            InputChip(
                selected = true,
                onClick = { onSelect(null) },
                label = { Text(Format.termName(selected, bangla)) },
                trailingIcon = { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_remove), modifier = Modifier.size(18.dp)) },
            )
        } else {
            SearchField(search, label, placeholder, error)
            Suggestions(search.results, bangla) {
                onSelect(it)
                search.clear()
            }
        }
    }
}

/** Pick any number of terms (the job's skills). */
@Composable
fun MultiTermPicker(
    search: TermSearch,
    selected: List<Term>,
    label: String,
    placeholder: String,
    onChange: (List<Term>) -> Unit,
) {
    val bangla = isBangla()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (selected.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                selected.forEach { term ->
                    InputChip(
                        selected = true,
                        onClick = { onChange(selected - term) },
                        label = { Text(Format.termName(term, bangla)) },
                        trailingIcon = { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_remove), modifier = Modifier.size(18.dp)) },
                    )
                }
            }
        }
        SearchField(search, label, placeholder, null)
        Suggestions(search.results.filter { result -> selected.none { it.id == result.id } }, bangla) {
            onChange(selected + it)
            search.clear()
        }
    }
}

@Composable
private fun SearchField(search: TermSearch, label: String, placeholder: String, error: String?) {
    OutlinedTextField(
        value = search.query,
        onValueChange = search::type,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Suggestions(results: List<Term>, bangla: Boolean, onPick: (Term) -> Unit) {
    if (results.isEmpty()) return
    Surface(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            results.forEachIndexed { index, term ->
                if (index > 0) HorizontalDivider()
                Column(Modifier.fillMaxWidth().clickable { onPick(term) }.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(Format.termName(term, bangla), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        Format.termName(term, !bangla),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
