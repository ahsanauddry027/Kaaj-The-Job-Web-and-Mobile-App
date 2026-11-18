package app.kaaj.android.ui.nav

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

// Type-safe Navigation Compose routes (one per screen).

// Employer
@Serializable object DashboardRoute
@Serializable object MyJobsRoute
@Serializable data class JobFormRoute(val jobId: String? = null)
@Serializable data class JobDetailRoute(val jobId: String)
@Serializable object ProfileRoute
@Serializable data class ApplicantsRoute(val jobId: String)
@Serializable data class ApplicantRoute(val applicationId: String)

// Job seeker
@Serializable object SearchRoute
@Serializable object SavedRoute
@Serializable object ApplicationsRoute
@Serializable object SeekerProfileRoute
@Serializable data class SeekerJobRoute(val jobId: String)
@Serializable data class ApplicationRoute(val applicationId: String)

// Admin
@Serializable object AdminQueueRoute
@Serializable data class AdminEmployerRoute(val employerId: String)

// Everyone
@Serializable object SettingsRoute

/** Bottom-bar navigation: one copy of each tab, keeping each tab's scroll position. */
fun NavHostController.navigateTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
