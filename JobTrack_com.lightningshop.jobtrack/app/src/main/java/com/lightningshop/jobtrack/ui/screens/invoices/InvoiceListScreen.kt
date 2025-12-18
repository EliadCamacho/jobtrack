package com.lightningshop.jobtrack.ui.screens.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.data.db.entities.InvoiceEntity
import com.lightningshop.jobtrack.data.repo.InvoicesRepository
import com.lightningshop.jobtrack.settings.AppPreferences
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
  prefs: AppPreferences,
  repo: InvoicesRepository,
  onOpenInvoice: (String) -> Unit,
  onNewInvoice: () -> Unit,
) {
  val invoices by repo.observeInvoices().collectAsState(initial = emptyList())

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = onNewInvoice) {
        Icon(Icons.Outlined.Add, contentDescription = "New invoice")
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
      Text("Invoices", style = MaterialTheme.typography.headlineLarge)

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        items(invoices, key = { it.id }) { inv ->
          InvoiceCard(inv = inv, onOpen = { onOpenInvoice(inv.id) })
        }
      }
    }
  }
}

@Composable
private fun InvoiceCard(inv: InvoiceEntity, onOpen: () -> Unit) {
  val df = rememberDf()
  Card(
    onClick = onOpen,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(inv.invoiceNumber, style = MaterialTheme.typography.titleLarge)
      val billTo = inv.billToName.ifBlank { "No bill-to yet" }
      Text(billTo, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Text("Issue ${inv.issueDate.format(df)}", style = MaterialTheme.typography.bodySmall)
      AssistChip(
        onClick = {},
        label = { Text(inv.status.name) },
        leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
      )
    }
  }
}

@Composable
private fun rememberDf(): DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
