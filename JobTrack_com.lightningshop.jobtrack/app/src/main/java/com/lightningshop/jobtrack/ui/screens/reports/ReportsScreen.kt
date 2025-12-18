package com.lightningshop.jobtrack.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.data.repo.InvoicesRepository
import com.lightningshop.jobtrack.data.repo.JobsRepository
import com.lightningshop.jobtrack.model.InvoiceStatus
import com.lightningshop.jobtrack.model.JobStatus
import com.lightningshop.jobtrack.settings.AppPreferences

@Composable
fun ReportsScreen(
  prefs: AppPreferences,
  jobsRepo: JobsRepository,
  invoicesRepo: InvoicesRepository,
) {
  val jobs by jobsRepo.observeJobs().collectAsState(initial = emptyList())
  val invoices by invoicesRepo.observeInvoices().collectAsState(initial = emptyList())

  val activeJobs = jobs.count { it.status == JobStatus.IN_PROGRESS || it.status == JobStatus.PLANNED || it.status == JobStatus.ON_HOLD }
  val doneJobs = jobs.count { it.status == JobStatus.DONE }
  val openInvoices = invoices.count { it.status != InvoiceStatus.PAID && it.status != InvoiceStatus.VOID }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text("Reports", style = MaterialTheme.typography.headlineLarge)

    StatCard(title = "Jobs", lines = listOf(
      "${jobs.size} total",
      "$activeJobs active",
      "$doneJobs completed",
    ))

    StatCard(title = "Invoices", lines = listOf(
      "${invoices.size} total",
      "$openInvoices open",
      "${invoices.count { it.status == InvoiceStatus.PAID }} paid",
    ))

    Card(Modifier.fillMaxWidth()) {
      Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Next: financial totals", style = MaterialTheme.typography.titleLarge)
        Text(
          "This screen is wired to your data. Next step is adding totals (revenue, costs, margin) and filters by month/customer.",
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}

@Composable
private fun StatCard(title: String, lines: List<String>) {
  Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(title, style = MaterialTheme.typography.titleLarge)
      lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
  }
}
