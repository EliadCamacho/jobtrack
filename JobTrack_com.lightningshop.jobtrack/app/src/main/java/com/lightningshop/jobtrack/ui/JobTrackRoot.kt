package com.lightningshop.jobtrack.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lightningshop.jobtrack.data.repo.InvoicesRepository
import com.lightningshop.jobtrack.data.repo.JobsRepository
import com.lightningshop.jobtrack.export.InvoiceExporter
import com.lightningshop.jobtrack.settings.AppPreferences
import com.lightningshop.jobtrack.ui.components.CornerCalculator
import com.lightningshop.jobtrack.ui.screens.invoices.InvoiceEditorScreen
import com.lightningshop.jobtrack.ui.screens.invoices.InvoiceListScreen
import com.lightningshop.jobtrack.ui.screens.jobs.JobDetailScreen
import com.lightningshop.jobtrack.ui.screens.jobs.JobsScreen
import com.lightningshop.jobtrack.ui.screens.reports.ReportsScreen
import com.lightningshop.jobtrack.ui.screens.settings.SettingsScreen

sealed class Route(val route: String, val label: String) {
  data object Jobs : Route("jobs", "Jobs")
  data object Invoices : Route("invoices", "Invoices")
  data object Reports : Route("reports", "Reports")
  data object Settings : Route("settings", "Settings")

  data object JobDetail : Route("job/{jobId}", "Job") {
    fun create(jobId: String) = "job/$jobId"
  }

  data object InvoiceEdit : Route("invoice/{invoiceId}", "Invoice") {
    fun create(invoiceId: String) = "invoice/$invoiceId"
  }
}

private data class BottomItem(
  val route: Route,
  val icon: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobTrackRoot(
  prefs: AppPreferences,
  jobsRepo: JobsRepository,
  invoicesRepo: InvoicesRepository,
  export: InvoiceExporter,
) {
  val nav = rememberNavController()
  val scope = rememberCoroutineScope()
  val backStack by nav.currentBackStackEntryAsState()
  val current = backStack?.destination?.route ?: Route.Jobs.route

  val themeState by prefs.themeState.collectAsState(initial = prefs.defaultThemeState())
  val showLabels = themeState.bottomLabels

  val bottomItems = remember {
    listOf(
      BottomItem(Route.Jobs) { Icon(Icons.Outlined.Folder, contentDescription = null) },
      BottomItem(Route.Invoices) { Icon(Icons.Outlined.Description, contentDescription = null) },
      BottomItem(Route.Reports) { Icon(Icons.Outlined.Summarize, contentDescription = null) },
      BottomItem(Route.Settings) { Icon(Icons.Outlined.Settings, contentDescription = null) },
    )
  }

  Scaffold(
    bottomBar = {
      val showBottom = current in setOf(
        Route.Jobs.route,
        Route.Invoices.route,
        Route.Reports.route,
        Route.Settings.route,
      )
      AnimatedVisibility(visible = showBottom) {
        NavigationBar {
          bottomItems.forEach { item ->
            val selected = current == item.route.route
            NavigationBarItem(
              selected = selected,
              onClick = {
                nav.navigate(item.route.route) {
                  popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              icon = { BadgedBox(badge = {}) { item.icon() } },
              label = if (showLabels) ({ Text(item.route.label) }) else null,
              alwaysShowLabel = showLabels,
            )
          }
        }
      }
    },
  ) { padding ->
    Box(Modifier.padding(padding)) {
      NavHost(navController = nav, startDestination = Route.Jobs.route) {
        composable(Route.Jobs.route) {
          JobsScreen(
            prefs = prefs,
            repo = jobsRepo,
            onOpenJob = { nav.navigate(Route.JobDetail.create(it)) },
            onCreateInvoiceForJob = { jobId ->
              // Create draft invoice then jump to editor
              scope.launch {
                val id = export.createDraftInvoiceForJob(jobId)
                nav.navigate(Route.InvoiceEdit.create(id))
              }
            },
          )
        }

        composable(Route.Invoices.route) {
          InvoiceListScreen(
            prefs = prefs,
            repo = invoicesRepo,
            onOpenInvoice = { nav.navigate(Route.InvoiceEdit.create(it)) },
            onNewInvoice = {
              scope.launch {
                val id = export.createDraftInvoiceForJob(null)
                nav.navigate(Route.InvoiceEdit.create(id))
              }
            },
          )
        }

        composable(Route.Reports.route) {
          ReportsScreen(prefs = prefs, jobsRepo = jobsRepo, invoicesRepo = invoicesRepo)
        }

        composable(Route.Settings.route) {
          SettingsScreen(prefs = prefs)
        }

        composable(Route.JobDetail.route) { entry ->
          val jobId = entry.arguments?.getString("jobId") ?: return@composable
          JobDetailScreen(
            prefs = prefs,
            repo = jobsRepo,
            jobId = jobId,
            onBack = { nav.popBackStack() },
          )
        }

        composable(Route.InvoiceEdit.route) { entry ->
          val invoiceId = entry.arguments?.getString("invoiceId") ?: return@composable
          InvoiceEditorScreen(
            prefs = prefs,
            repo = invoicesRepo,
            jobsRepo = jobsRepo,
            export = export,
            invoiceId = invoiceId,
            onBack = { nav.popBackStack() },
          )
        }
      }

      CornerCalculator(prefs = prefs)
    }
  }
}
