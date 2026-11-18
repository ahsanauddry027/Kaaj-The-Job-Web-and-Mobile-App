package app.kaaj.android.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.BuildConfig
import app.kaaj.android.R
import app.kaaj.android.data.AppContainer
import app.kaaj.android.data.model.Role
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.FieldError
import app.kaaj.android.domain.Validation
import app.kaaj.android.ui.ServerAddressDialog
import app.kaaj.android.ui.common.ChoiceChips
import app.kaaj.android.ui.common.FormField
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.errorText
import app.kaaj.android.ui.common.text
import app.kaaj.android.ui.theme.Brand
import kotlinx.coroutines.launch

/** Sign in and create account (wireframe screens E01/E02 and S01/S02). */
class AuthViewModel(private val container: AppContainer) : KaajViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var role by mutableStateOf(Role.EMPLOYER)
    var errors by mutableStateOf<Map<String, FieldError>>(emptyMap())
        private set
    var serverErrors by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var failure by mutableStateOf<ApiException?>(null)
        private set
    var busy by mutableStateOf(false)
        private set

    fun clearErrors() {
        errors = emptyMap()
        serverErrors = emptyMap()
        failure = null
    }

    fun signIn() {
        clearErrors()
        errors = Validation.signIn(email, password)
        if (errors.isNotEmpty()) return
        submit { container.auth.signIn(email, password) }
    }

    fun register() {
        clearErrors()
        errors = Validation.register(email, password)
        if (errors.isNotEmpty()) return
        submit { container.auth.register(email, password, role) }
    }

    private fun submit(call: suspend () -> app.kaaj.android.data.model.Me) {
        busy = true
        viewModelScope.launch {
            try {
                container.session.signedIn(call())
            } catch (e: ApiException) {
                serverErrors = e.fieldErrors
                failure = e
            } finally {
                busy = false
            }
        }
    }
}

@Composable
fun AuthFlow() {
    val container = LocalAppContainer.current
    val vm: AuthViewModel = viewModel { AuthViewModel(container) }
    var registering by rememberSaveable { mutableStateOf(false) }
    var editingServer by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val expired by container.session.expired.collectAsStateWithLifecycle()
    val context = LocalContext.current
    MessageEffect(vm.messages, snackbar)

    LaunchedEffect(expired) {
        if (expired) {
            snackbar.showSnackbar(context.getString(R.string.common_session_expired))
            container.session.expiredNoticeShown()
        }
    }
    BackHandler(enabled = registering) {
        registering = false
        vm.clearErrors()
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Logo()
            Text(stringResource(R.string.app_tagline), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            SectionCard(title = stringResource(if (registering) R.string.auth_register_title else R.string.auth_sign_in_title)) {
                if (registering) {
                    Text(stringResource(R.string.auth_i_want_to), style = MaterialTheme.typography.labelLarge)
                    ChoiceChips(
                        options = listOf(Role.SEEKER, Role.EMPLOYER),
                        selected = vm.role,
                        label = { stringResource(if (it == Role.SEEKER) R.string.auth_find_job else R.string.auth_hire) },
                        onSelect = { vm.role = it },
                    )
                }
                FormField(
                    value = vm.email,
                    onValueChange = { vm.email = it },
                    label = stringResource(R.string.auth_email),
                    keyboardType = KeyboardType.Email,
                    error = vm.errors["email"]?.text() ?: vm.serverErrors["email"],
                )
                PasswordField(
                    value = vm.password,
                    onValueChange = { vm.password = it },
                    error = vm.errors["password"]?.text() ?: vm.serverErrors["password"],
                    hint = if (registering) stringResource(R.string.v_password_length) else null,
                )
                vm.failure?.let { failure ->
                    if (failure.fieldErrors.isEmpty()) {
                        Text(errorText(failure), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Button(
                    onClick = { if (registering) vm.register() else vm.signIn() },
                    enabled = !vm.busy,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (vm.busy) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(if (registering) R.string.auth_register else R.string.auth_sign_in))
                    }
                }
                TextButton(
                    onClick = { registering = !registering; vm.clearErrors() },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(stringResource(if (registering) R.string.auth_have_account else R.string.auth_no_account))
                }
            }

            if (BuildConfig.DEBUG && !registering) {
                DemoAccounts(onPick = { email, password ->
                    vm.email = email
                    vm.password = password
                    vm.clearErrors()
                })
            }

            TextButton(onClick = { editingServer = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.auth_server, container.settings.serverUrl), style = MaterialTheme.typography.bodySmall)
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).safeDrawingPadding())
    }

    if (editingServer) {
        ServerAddressDialog(onDismiss = { editingServer = false }, onSaved = { editingServer = false })
    }
}

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(52.dp).background(Brand, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_launcher_foreground), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(52.dp))
        }
        Column {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Brand)
            Text(stringResource(R.string.app_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, error: String?, hint: String?) {
    var visible by rememberSaveable { mutableStateOf(false) }
    FormField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.auth_password),
        keyboardType = KeyboardType.Password,
        error = error,
        supporting = hint,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailing = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    painterResource(if (visible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                    contentDescription = stringResource(R.string.auth_show_password),
                )
            }
        },
    )
}

/** Debug builds only: the demo accounts from db/demo (R__demo_member3.sql, R__demo_seeker.sql), one tap to fill the form. */
@Composable
private fun DemoAccounts(onPick: (String, String) -> Unit) {
    // The admin is the dev-profile default from application-dev.properties (or your .env values).
    val accounts = listOf(
        Triple("karim@demo.kaaj", "Kaaj@12345", R.string.role_employer),
        Triple("tongigarments@demo.kaaj", "Kaaj@12345", R.string.role_employer),
        Triple("rahim@demo.kaaj", "Kaaj@12345", R.string.role_seeker),
        Triple("sumi@demo.kaaj", "Kaaj@12345", R.string.role_seeker),
        Triple("admin@kaaj.local", "Admin@12345", R.string.role_admin),
    )
    SectionCard(title = stringResource(R.string.auth_demo_title)) {
        accounts.forEachIndexed { index, (email, password, role) ->
            if (index > 0) HorizontalDivider()
            Row(
                Modifier.fillMaxWidth().clickable { onPick(email, password) }.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(email, style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(role), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(stringResource(R.string.auth_demo_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
