package app.kaaj.android.ui.admin

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.kaaj.android.data.model.Me
import app.kaaj.android.ui.nav.AdminEmployerRoute
import app.kaaj.android.ui.nav.AdminQueueRoute
import app.kaaj.android.ui.nav.SettingsRoute
import app.kaaj.android.ui.settings.SettingsScreen

/** The admin's app: the employer verification queue (wireframe A04/A05). */
@Composable
fun AdminApp(me: Me) {
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        NavHost(
            navController = nav,
            startDestination = AdminQueueRoute,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable<AdminQueueRoute> {
                AdminQueueScreen(
                    snackbar = snackbar,
                    onOpen = { nav.navigate(AdminEmployerRoute(it)) },
                    onSettings = { nav.navigate(SettingsRoute) },
                )
            }
            composable<AdminEmployerRoute> { entry ->
                AdminEmployerScreen(
                    employerId = entry.toRoute<AdminEmployerRoute>().employerId,
                    snackbar = snackbar,
                    onBack = { nav.popBackStack() },
                )
            }
            composable<SettingsRoute> {
                SettingsScreen(me = me, onBack = { nav.popBackStack() })
            }
        }
    }
}
