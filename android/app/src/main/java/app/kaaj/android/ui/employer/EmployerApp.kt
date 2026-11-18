package app.kaaj.android.ui.employer

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.kaaj.android.R
import app.kaaj.android.data.model.Me
import app.kaaj.android.ui.applicants.ApplicantScreen
import app.kaaj.android.ui.applicants.ApplicantsScreen
import app.kaaj.android.ui.jobs.JobDetailScreen
import app.kaaj.android.ui.jobs.JobFormScreen
import app.kaaj.android.ui.jobs.MyJobsScreen
import app.kaaj.android.ui.nav.ApplicantRoute
import app.kaaj.android.ui.nav.ApplicantsRoute
import app.kaaj.android.ui.nav.DashboardRoute
import app.kaaj.android.ui.nav.JobDetailRoute
import app.kaaj.android.ui.nav.JobFormRoute
import app.kaaj.android.ui.nav.MyJobsRoute
import app.kaaj.android.ui.nav.ProfileRoute
import app.kaaj.android.ui.nav.SettingsRoute
import app.kaaj.android.ui.nav.navigateTab
import app.kaaj.android.ui.settings.SettingsScreen

private class Tab(
    val route: Any,
    val label: Int,
    val icon: ImageVector,
    val matches: (NavDestination) -> Boolean,
)

/** The employer's app: Dashboard, My jobs, Post a job and My business, with a bottom bar. */
@Composable
fun EmployerApp(me: Me) {
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val entry by nav.currentBackStackEntryAsState()
    val destination = entry?.destination

    val tabs = listOf(
        Tab(DashboardRoute, R.string.nav_dashboard, Icons.Filled.Home) { it.hasRoute<DashboardRoute>() },
        Tab(MyJobsRoute, R.string.nav_my_jobs, Icons.AutoMirrored.Filled.List) { it.hasRoute<MyJobsRoute>() },
        Tab(JobFormRoute(), R.string.nav_post_short, Icons.Filled.AddCircle) { it.hasRoute<JobFormRoute>() },
        Tab(ProfileRoute, R.string.nav_business, Icons.Filled.AccountBox) { it.hasRoute<ProfileRoute>() },
    )
    val showBar = destination == null || tabs.any { tab -> tab.matches(destination) }

    fun openNewJobForm() {
        nav.navigate(JobFormRoute()) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { tab.matches(it) } == true,
                            onClick = { if (tab.route is JobFormRoute) openNewJobForm() else nav.navigateTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label), maxLines = 1) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable<DashboardRoute> {
                DashboardScreen(
                    snackbar = snackbar,
                    onOpenProfile = { nav.navigateTab(ProfileRoute) },
                    onPostJob = ::openNewJobForm,
                    onOpenJob = { nav.navigate(JobDetailRoute(it)) },
                    onSeeAll = { nav.navigateTab(MyJobsRoute) },
                    onSettings = { nav.navigate(SettingsRoute) },
                )
            }
            composable<MyJobsRoute> {
                MyJobsScreen(
                    snackbar = snackbar,
                    onOpenJob = { nav.navigate(JobDetailRoute(it)) },
                    onEditJob = { nav.navigate(JobFormRoute(it)) },
                    onPostJob = ::openNewJobForm,
                    onSettings = { nav.navigate(SettingsRoute) },
                )
            }
            composable<JobFormRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<JobFormRoute>()
                JobFormScreen(
                    jobId = route.jobId,
                    snackbar = snackbar,
                    onBack = { nav.popBackStack() },
                    onSaved = { job ->
                        nav.navigate(JobDetailRoute(job)) {
                            popUpTo<JobFormRoute> { inclusive = true }
                        }
                    },
                    onOpenProfile = { nav.navigateTab(ProfileRoute) },
                )
            }
            composable<JobDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<JobDetailRoute>()
                JobDetailScreen(
                    jobId = route.jobId,
                    snackbar = snackbar,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate(JobFormRoute(it)) },
                    onDeleted = { nav.popBackStack() },
                    onReposted = { newDraft ->
                        nav.navigate(JobFormRoute(newDraft)) {
                            popUpTo<JobDetailRoute> { inclusive = true }
                        }
                    },
                    onApplicants = { nav.navigate(ApplicantsRoute(it)) },
                )
            }
            composable<ApplicantsRoute> { backStackEntry ->
                ApplicantsScreen(
                    jobId = backStackEntry.toRoute<ApplicantsRoute>().jobId,
                    snackbar = snackbar,
                    onBack = { nav.popBackStack() },
                    onOpen = { nav.navigate(ApplicantRoute(it)) },
                )
            }
            composable<ApplicantRoute> { backStackEntry ->
                ApplicantScreen(
                    applicationId = backStackEntry.toRoute<ApplicantRoute>().applicationId,
                    snackbar = snackbar,
                    onBack = { nav.popBackStack() },
                )
            }
            composable<ProfileRoute> {
                ProfileScreen(snackbar = snackbar, onSettings = { nav.navigate(SettingsRoute) })
            }
            composable<SettingsRoute> {
                SettingsScreen(me = me, onBack = { nav.popBackStack() })
            }
        }
    }
}
