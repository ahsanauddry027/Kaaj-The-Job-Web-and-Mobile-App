package app.kaaj.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kaaj.android.BuildConfig
import app.kaaj.android.R
import app.kaaj.android.data.AppLanguage
import app.kaaj.android.data.model.Me
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.ServerAddress
import app.kaaj.android.ui.common.ChoiceChips
import app.kaaj.android.ui.common.FormField
import app.kaaj.android.ui.common.InfoRow
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.errorText
import app.kaaj.android.ui.common.label
import kotlinx.coroutines.launch

/** Language, server address, account and sign-out — the same for every role. */
@Composable
fun SettingsScreen(me: Me, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var server by remember { mutableStateOf(container.settings.serverUrl) }
    var serverNote by remember { mutableStateOf<String?>(null) }
    var serverError by remember { mutableStateOf<String?>(null) }
    var language by remember { mutableStateOf(AppLanguage.chosen()) }
    val savedText = stringResource(R.string.settings_server_saved)
    val invalidText = stringResource(R.string.settings_server_invalid)
    val okText = stringResource(R.string.settings_test_ok)
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(topBar = { KaajTopBar(title = stringResource(R.string.settings_title), onBack = onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(title = stringResource(R.string.settings_language)) {
                ChoiceChips(
                    options = listOf("", "bn", "en"),
                    selected = language,
                    label = {
                        when (it) {
                            "bn" -> stringResource(R.string.lang_bn)
                            "en" -> stringResource(R.string.lang_en)
                            else -> stringResource(R.string.settings_language_system)
                        }
                    },
                    onSelect = {
                        language = it
                        AppLanguage.choose(it) // AppCompat recreates the screen in the new language
                    },
                )
            }

            SectionCard(title = stringResource(R.string.settings_server)) {
                FormField(
                    value = server,
                    onValueChange = {
                        server = it
                        serverError = null
                        serverNote = null
                    },
                    label = stringResource(R.string.settings_server),
                    supporting = serverNote ?: stringResource(R.string.settings_server_hint),
                    error = serverError,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val address = ServerAddress.normalise(server)
                        if (address == null) {
                            serverError = invalidText
                        } else {
                            container.settings.serverUrl = address
                            server = address
                            serverNote = savedText
                        }
                    }) { Text(stringResource(R.string.settings_server_save)) }
                    OutlinedButton(onClick = {
                        scope.launch {
                            serverNote = try {
                                container.auth.ping()
                                okText
                            } catch (e: ApiException) {
                                errorText(context, e)
                            }
                        }
                    }) { Text(stringResource(R.string.settings_test)) }
                }
            }

            SectionCard(title = stringResource(R.string.settings_account)) {
                InfoRow(stringResource(R.string.auth_email), me.email)
                InfoRow(stringResource(R.string.settings_role), me.role.label())
                Button(
                    onClick = {
                        scope.launch {
                            container.auth.signOut()
                            container.session.signedOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(painterResource(R.drawable.ic_logout), contentDescription = null)
                    Text(stringResource(R.string.nav_sign_out), modifier = Modifier.padding(start = 8.dp))
                }
            }

            SectionCard(title = stringResource(R.string.settings_about)) {
                Text(stringResource(R.string.app_name) + " — " + stringResource(R.string.app_subtitle))
                Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
