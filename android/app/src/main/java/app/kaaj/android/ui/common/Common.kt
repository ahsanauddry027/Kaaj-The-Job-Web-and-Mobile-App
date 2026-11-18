@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package app.kaaj.android.ui.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.kaaj.android.R
import app.kaaj.android.data.AppContainer
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.FieldError
import kotlinx.coroutines.flow.Flow

/** The app's objects (repositories, settings, session), provided by MainActivity. */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> { error("AppContainer not provided") }

/** True when the app is showing Bangla, so numbers and dates use Bangla digits. */
@Composable
@ReadOnlyComposable
fun isBangla(): Boolean = LocalConfiguration.current.locales[0].language == "bn"

/** A one-off message for the snackbar, created in a ViewModel without needing a Context. */
sealed interface UiMessage {
    data class Res(@StringRes val id: Int) : UiMessage
    data class Error(val error: ApiException) : UiMessage

    fun resolve(context: Context): String = when (this) {
        is Res -> context.getString(id)
        is Error -> errorText(context, error)
    }
}

fun errorText(context: Context, error: ApiException): String = when {
    error.isNetwork -> context.getString(R.string.common_network_error)
    error.status == 401 -> context.getString(R.string.common_session_expired)
    !error.message.isNullOrBlank() -> error.message!!
    else -> context.getString(R.string.common_something_wrong)
}

@Composable
fun errorText(error: ApiException): String = errorText(LocalContext.current, error)

/** Shows every message from [messages] in [snackbar]. */
@Composable
fun MessageEffect(messages: Flow<UiMessage>, snackbar: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(messages) {
        messages.collect { snackbar.showSnackbar(it.resolve(context)) }
    }
}

@Composable
fun FieldError.text(): String = stringResource(
    when (this) {
        FieldError.REQUIRED -> R.string.v_required
        FieldError.TOO_LONG -> R.string.v_too_long
        FieldError.NAME_LENGTH -> R.string.v_name_length
        FieldError.PHONE -> R.string.v_phone
        FieldError.EMAIL -> R.string.v_email
        FieldError.PASSWORD_LENGTH -> R.string.v_password_length
        FieldError.TITLE_REQUIRED -> R.string.v_title_required
        FieldError.TITLE_SHORT -> R.string.v_title_short
        FieldError.DESCRIPTION_REQUIRED -> R.string.v_description_required
        FieldError.CATEGORY_REQUIRED -> R.string.v_category_required
        FieldError.NUMBER -> R.string.v_number
        FieldError.OPENINGS_RANGE -> R.string.v_openings_range
        FieldError.WAGE_REQUIRED -> R.string.v_wage_required
        FieldError.WAGE_POSITIVE -> R.string.v_wage_positive
        FieldError.WAGE_RANGE -> R.string.v_wage_range
        FieldError.PERIOD_REQUIRED -> R.string.v_period_required
        FieldError.LOCATION_REQUIRED -> R.string.v_location_required
        FieldError.HOURS_REQUIRED -> R.string.v_hours_required
        FieldError.EXPERIENCE_RANGE -> R.string.v_experience_range
        FieldError.YEARS_RANGE -> R.string.v_years_range
    },
)

/** The top bar used on every screen. */
@Composable
fun KaajTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, maxLines = 1, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_navigate_back))
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** A full-screen error with a "Try again" button. */
@Composable
fun ErrorState(error: ApiException, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
        Text(
            errorText(error),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
    }
}

/** A white card with an optional title, the building block of every screen. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

/** A text field with the error (or a hint) under it. */
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String? = null,
    supporting: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        supportingText = (error ?: supporting)?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Pick one value from a short list, shown as chips that wrap onto new lines. */
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
            )
        }
    }
}

/** Pick any number of values from a short list. */
@Composable
fun <T> MultiChoiceChips(
    options: List<T>,
    selected: List<T>,
    label: @Composable (T) -> String,
    onToggle: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option in selected
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(option) },
                label = { Text(label(option)) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
            )
        }
    }
}

/** A small coloured label, for statuses and badges. */
@Composable
fun Pill(text: String, background: Color, content: Color, modifier: Modifier = Modifier, icon: (@Composable () -> Unit)? = null) {
    Surface(color = background, contentColor = content, shape = RoundedCornerShape(50), modifier = modifier) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Box(Modifier.size(4.dp))
            }
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** A coloured box with a title and a sentence: verification banners and tips. */
@Composable
fun Banner(title: String?, body: String, background: Color, content: Color, modifier: Modifier = Modifier, icon: (@Composable () -> Unit)? = null) {
    Surface(color = background, contentColor = content, shape = RoundedCornerShape(14.dp), modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (icon != null) icon()
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (title != null) Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** A label/value row inside a card. */
@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.42f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.58f))
    }
}
