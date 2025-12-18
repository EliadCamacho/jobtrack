package com.lightningshop.jobtrack.ui.screens.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.data.db.entities.JobEntity
import com.lightningshop.jobtrack.data.repo.JobsRepository
import com.lightningshop.jobtrack.model.JobStatus
import com.lightningshop.jobtrack.settings.AppPreferences
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(
  prefs: AppPreferences,
  repo: JobsRepository,
  onOpenJob: (String) -> Unit,
  onCreateInvoiceForJob: (String) -> Unit,
) {
  val scope = rememberCoroutineScope()
  val jobs by repo.observeJobs().collectAsState(initial = emptyList())

  var query by remember { mutableStateOf("") }
  var showCreate by remember { mutableStateOf(false) }

  val filtered = remember(jobs, query) {
    val q = query.trim().lowercase()
    if (q.isBlank()) jobs
    else jobs.filter {
      it.title.lowercase().contains(q) ||
        it.contractor.lowercase().contains(q) ||
        it.customer.lowercase().contains(q) ||
        it.address.lowercase().contains(q)
    }
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = { showCreate = true }) {
        Icon(Icons.Outlined.Add, contentDescription = "Add job")
      }
    },
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text("Jobs", style = MaterialTheme.typography.headlineLarge)

      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        label = { Text("Search jobs") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        items(filtered, key = { it.id }) { job ->
          JobCard(
            job = job,
            onOpen = { onOpenJob(job.id) },
            onInvoice = { onCreateInvoiceForJob(job.id) },
          )
        }
      }
    }

    if (showCreate) {
      CreateJobDialog(
        onDismiss = { showCreate = false },
        onCreate = { title, contractor, customer, address ->
          scope.launch {
            repo.upsertJob(
              JobEntity(
                title = title,
                contractor = contractor,
                customer = customer,
                address = address,
                status = JobStatus.PLANNED,
                startDate = LocalDate.now(),
              ),
            )
            showCreate = false
          }
        },
      )
    }
  }
}

@Composable
private fun JobCard(
  job: JobEntity,
  onOpen: () -> Unit,
  onInvoice: () -> Unit,
) {
  Card(
    onClick = onOpen,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
          Text(job.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
          val subtitle = listOf(job.contractor, job.customer).filter { it.isNotBlank() }.joinToString(" • ")
          if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
        AssistChip(
          onClick = {},
          label = { Text(job.status.name.replace("_", " ")) },
          colors = AssistChipDefaults.assistChipColors(),
        )
      }

      if (job.address.isNotBlank()) {
        Text(job.address, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onInvoice) {
          Icon(Icons.Outlined.ReceiptLong, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text("Invoice")
        }
      }
    }
  }
}
