package app.kaaj.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.AppContainer
import app.kaaj.android.data.Session
import app.kaaj.android.data.model.Role
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.ServerAddress
import app.kaaj.android.ui.admin.AdminApp
import app.kaaj.android.ui.auth.AuthFlow
import app.kaaj.android.ui.common.FormField
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.errorText
import app.kaaj.android.ui.employer.EmployerApp
import app.kaaj.android.ui.seeker.SeekerApp
import kotlinx.coroutines.launch

/** Checks for a saved session when the app starts. */
class RootViewModel(private val container: AppContainer) : KaajViewModel() {
    var startupError by mutableStateOf<ApiException?>(null)
        private set

    init {
        restore()
    }

    fun restore() {
        startupError = null
        viewModelScope.launch {
            try {
                val me = container.auth.currentUser()
                if (me != null) container.session.signedIn(me) else container.session.signedOut()
            } catch (e: ApiException) {
                startupError = e
            }
        }
    }
}

/** Picks what to show: the sign-in screens, or the app for the signed-in user's role. */
@Composable
fun KaajRoot() {
    val container = LocalAppContainer.current
    val root: RootViewModel = viewModel { RootViewModel(container) }
    val session by container.session.state.collectAsStateWithLifecycle()

    when (val current = session) {
        Session.Loading -> {
            val error = root.startupError
            if (error == null) LoadingBox() else StartupError(error, onRetry = root::restore)
        }
        Session.SignedOut -> AuthFlow()
        is Session.SignedIn -> key(current.me.id) {
            when (current.me.role) {
                Role.EMPLOYER -> EmployerApp(current.me)
                Role.ADMIN -> AdminApp(current.me)
                Role.SEEKER -> SeekerApp(current.me)
            }
        }
    }
}

/** The server can't be reached at start-up: let the user fix the address and try again. */
@Composable
private fun StartupError(error: ApiException, onRetry: () -> Unit) {
    var editing by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Text(
            errorText(error),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
        TextButton(onClick = { editing = true }) { Text(stringResource(R.string.settings_server)) }
    }
    if (editing) {
        ServerAddressDialog(onDismiss = { editing = false }, onSaved = { editing = false; onRetry() })
    }
}

/** Change the backend address (emulator: http://10.0.2.2:1027, phone: your computer's Wi-Fi IP). */
@Composable
fun ServerAddressDialog(onDismiss: () -> Unit, onSaved: () -> Unit) {
    val settings = LocalAppContainer.current.settings
    var text by remember { mutableStateOf(settings.serverUrl) }
    var invalid by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_server)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField(
                    value = text,
                    onValueChange = { text = it; invalid = false },
                    label = stringResource(R.string.settings_server),
                    error = if (invalid) stringResource(R.string.settings_server_invalid) else null,
                    supporting = stringResource(R.string.settings_server_hint),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val address = ServerAddress.normalise(text)
                if (address == null) {
                    invalid = true
                } else {
                    settings.serverUrl = address
                    onSaved()
                }
            }) { Text(stringResource(R.string.settings_server_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}
