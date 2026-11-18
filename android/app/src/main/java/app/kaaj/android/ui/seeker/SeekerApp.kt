package app.kaaj.android.ui.seeker

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.kaaj.android.R
import app.kaaj.android.data.model.Me
import app.kaaj.android.ui.nav.ApplicationRoute
import app.kaaj.android.ui.nav.ApplicationsRoute
import app.kaaj.android.ui.nav.SavedRoute
import app.kaaj.android.ui.nav.SearchRoute
import app.kaaj.android.ui.nav.SeekerJobRoute
import app.kaaj.android.ui.nav.SeekerProfileRoute
import app.kaaj.android.ui.nav.SettingsRoute
import app.kaaj.android.ui.nav.navigateTab
import app.kaaj.android.ui.settings.SettingsScreen

private class SeekerTab(val route: Any, val label: Int, val icon: ImageVector?, val drawable: Int?, val matches: (NavDestination) -> Boolean)

/** The job seeker's app: Find jobs, Saved, Applications and Profile, with a bottom bar. */
@Composable
fun SeekerApp(me: Me) {
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination

    val tabs = listOf(
        SeekerTab(SearchRoute, R.string.nav_find_jobs, Icons.Filled.Search, null) { it.hasRoute<SearchRoute>() },
        SeekerTab(SavedRoute, R.string.nav_saved, Icons.Filled.Star, null) { it.hasRoute<SavedRoute>() },
        SeekerTab(ApplicationsRoute, R.string.nav_applications, null, R.drawable.ic_send) { it.hasRoute<ApplicationsRoute>() },
        SeekerTab(SeekerProfileRoute, R.string.nav_profile, Icons.Filled.AccountCircle, null) { it.hasRoute<SeekerProfileRoute>() },
    )
    val showBar = destination == null || tabs.any { it.matches(destination) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { tab.matches(it) } == true,
                            onClick = { nav.navigateTab(tab.route) },
                            icon = {
                                if (tab.icon != null) Icon(tab.icon, contentDescription = null)
                                else if (tab.drawable != null) Icon(painterResource(tab.drawable), contentDescription = null)
                            },
                            label = { Text(stringResource(tab.label), maxLines = 1) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = SearchRoute,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable<SearchRoute> {
                SearchScreen(snackbar, onOpenJob = { nav.navigate(SeekerJobRoute(it)) }, onSettings = { nav.navigate(SettingsRoute) })
            }
            composable<SavedRoute> {
                SavedScreen(snackbar, onOpenJob = { nav.navigate(SeekerJobRoute(it)) })
            }
            composable<ApplicationsRoute> {
                ApplicationsScreen(snackbar, onOpen = { nav.navigate(ApplicationRoute(it)) })
            }
            composable<SeekerProfileRoute> {
                SeekerProfileScreen(snackbar, onSettings = { nav.navigate(SettingsRoute) })
            }
            composable<SeekerJobRoute> { backStackEntry ->
                SeekerJobScreen(
                    jobId = backStackEntry.toRoute<SeekerJobRoute>().jobId,
                    snackbar = snackbar,
                    onBack = { nav.popBackStack() },
                    onOpenApplication = { nav.navigate(ApplicationRoute(it)) },
                    onOpenProfile = { nav.navigateTab(SeekerProfileRoute) },
                )
            }
            composable<ApplicationRoute> { backStackEntry ->
                ApplicationScreen(
                    applicationId = backStackEntry.toRoute<ApplicationRoute>().applicationId,
                    snackbar = snackbar,
                    onBack = { nav.popBackStack() },
                    onOpenJob = { nav.navigate(SeekerJobRoute(it)) },
                )
            }
            composable<SettingsRoute> {
                SettingsScreen(me = me, onBack = { nav.popBackStack() })
            }
        }
    }
}
