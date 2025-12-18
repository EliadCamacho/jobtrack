package com.lightningshop.jobtrack.ui.screens.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.data.db.entities.ExpenseEntity
import com.lightningshop.jobtrack.data.db.entities.WorkLogEntity
import com.lightningshop.jobtrack.data.repo.JobsRepository
import com.lightningshop.jobtrack.model.formatCents
import com.lightningshop.jobtrack.settings.AppPreferences
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class JobTab(val label: String) {
  OVERVIEW("Overview"),
  EXPENSES("Expenses"),
  WORK("Work"),
  NOTES("Notes"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
  prefs: AppPreferences,
  repo: JobsRepository,
  jobId: String,
  onBack: () -> Unit,
) {
  val scope = rememberCoroutineScope()

  val job by repo.observeJob(jobId).collectAsState(initial = null)
  val expenses by repo.observeExpenses(jobId).collectAsState(initial = emptyList())
  val workLogs by repo.observeWorkLogs(jobId).collectAsState(initial = emptyList())
  val totalExpenses by repo.observeJobSummary(jobId).collectAsState(initial = null)

  val currency by prefs.currencyCode.collectAsState(initial = "USD")

  var tabIndex by remember { mutableIntStateOf(0) }
  val tabs = remember { JobTab.entries.toList() }

  var showAddExpense by remember { mutableStateOf(false) }
  var showAddWork by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            job?.title ?: "Job",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
        },
        actions = {
          IconButton(
            onClick = {
              when (tabs[tabIndex]) {
                JobTab.EXPENSES -> showAddExpense = true
                JobTab.WORK -> showAddWork = true
                else -> {}
              }
            },
          ) { Icon(Icons.Outlined.Add, contentDescription = "Add") }
        },
      )
    },
  ) { padding ->
    Column(Modifier.padding(padding).fillMaxSize()) {
      TabRow(selectedTabIndex = tabIndex) {
        tabs.forEachIndexed { idx, t ->
          Tab(
            selected = idx == tabIndex,
            onClick = { tabIndex = idx },
            text = { Text(t.label) },
          )
        }
      }

      when (tabs[tabIndex]) {
        JobTab.OVERVIEW -> OverviewTab(
          prefs = prefs,
          repo = repo,
          jobId = jobId,
          onDelete = {
            scope.launch { repo.deleteJob(jobId); onBack() }
          },
        )

        JobTab.EXPENSES -> ExpensesTab(
          currency = currency,
          expenses = expenses,
          onDelete = { id -> scope.launch { repo.deleteExpense(id) } },
        )

        JobTab.WORK -> WorkTab(
          currency = currency,
          logs = workLogs,
          onDelete = { id -> scope.launch { repo.deleteWorkLog(id) } },
        )

        JobTab.NOTES -> NotesTab(
          jobNotes = job?.notes ?: "",
          onSave = { newNotes ->
            scope.launch {
              val j = repo.getJob(jobId) ?: return@launch
              repo.upsertJob(j.copy(notes = newNotes, updatedAt = System.currentTimeMillis()))
            }
          },
        )
      }
    }

    if (showAddExpense) {
      AddExpenseDialog(
        currency = currency,
        onDismiss = { showAddExpense = false },
        onAdd = { type, vendor, desc, cents, category ->
          scope.launch {
            repo.addExpense(
              ExpenseEntity(
                jobId = jobId,
                type = type,
                vendor = vendor,
                description = desc,
                amountCents = cents,
                category = category,
                date = LocalDate.now(),
              ),
            )
            showAddExpense = false
          }
        },
      )
    }

    if (showAddWork) {
      AddWorkLogDialog(
        currency = currency,
        onDismiss = { showAddWork = false },
        onAdd = { worker, hours, rateCents, note ->
          scope.launch {
            repo.addWorkLog(
              WorkLogEntity(
                jobId = jobId,
                workerName = worker,
                hours = hours,
                rateCentsPerHour = rateCents,
                note = note,
                date = LocalDate.now(),
              ),
            )
            showAddWork = false
          }
        },
      )
    }
  }
}

@Composable
private fun OverviewTab(
  prefs: AppPreferences,
  repo: JobsRepository,
  jobId: String,
  onDelete: () -> Unit,
) {
  val summary by repo.observeJobSummary(jobId).collectAsState(initial = null)
  val currency by prefs.currencyCode.collectAsState(initial = "USD")

  Column(
    Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text("Financial snapshot", style = MaterialTheme.typography.titleLarge)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text("Expenses")
      Text(formatCents(summary?.totalExpensesCents ?: 0, currency))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text("Labor (work logs)")
      Text(formatCents(summary?.totalLaborCents ?: 0, currency))
    }
    Spacer(Modifier.padding(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text("Total cost", style = MaterialTheme.typography.titleMedium)
      val total = (summary?.totalExpensesCents ?: 0) + (summary?.totalLaborCents ?: 0)
      Text(formatCents(total, currency), style = MaterialTheme.typography.titleMedium)
    }

    Spacer(Modifier.weight(1f))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = "Delete job") }
    }
  }
}

@Composable
private fun ExpensesTab(
  currency: String,
  expenses: List<ExpenseEntity>,
  onDelete: (String) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(expenses, key = { it.id }) { e ->
      ExpenseRow(currency = currency, e = e, onDelete = { onDelete(e.id) })
    }
  }
}

@Composable
private fun ExpenseRow(currency: String, e: ExpenseEntity, onDelete: () -> Unit) {
  androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
    Row(
      Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(e.type.name.replace("_", " "), style = MaterialTheme.typography.labelLarge)
        Text(e.description, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        val sub = listOf(e.vendor, e.category).filter { it.isNotBlank() }.joinToString(" • ")
        if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall)
      }
      Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
        Text(formatCents(e.amountCents, currency), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = null) }
      }
    }
  }
}

@Composable
private fun WorkTab(
  currency: String,
  logs: List<WorkLogEntity>,
  onDelete: (String) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    items(logs, key = { it.id }) { w ->
      WorkLogRow(currency = currency, w = w, onDelete = { onDelete(w.id) })
    }
  }
}

@Composable
private fun WorkLogRow(currency: String, w: WorkLogEntity, onDelete: () -> Unit) {
  val laborCents = ((w.hours * (w.rateCentsPerHour / 100.0)) * 100).toLong()
  androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
    Row(
      Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(w.workerName, style = MaterialTheme.typography.titleMedium)
        Text("${w.hours} h @ ${formatCents(w.rateCentsPerHour, currency)}/h", style = MaterialTheme.typography.bodySmall)
        if (w.note.isNotBlank()) Text(w.note, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
      Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
        Text(formatCents(laborCents, currency), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = null) }
      }
    }
  }
}

@Composable
private fun NotesTab(
  jobNotes: String,
  onSave: (String) -> Unit,
) {
  var notes by remember(jobNotes) { mutableStateOf(jobNotes) }

  Column(
    Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    OutlinedTextField(
      value = notes,
      onValueChange = { notes = it },
      label = { Text("Notes") },
      modifier = Modifier.fillMaxWidth().weight(1f),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      TextButton(onClick = { onSave(notes) }) { Text("Save") }
    }
  }
}
