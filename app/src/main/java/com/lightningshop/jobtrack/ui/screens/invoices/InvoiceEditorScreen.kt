package com.lightningshop.jobtrack.ui.screens.invoices

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.assistChipColors
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.data.db.entities.InvoiceEntity
import com.lightningshop.jobtrack.data.db.entities.InvoiceLineEntity
import com.lightningshop.jobtrack.data.db.entities.PaymentEntity
import com.lightningshop.jobtrack.data.repo.InvoicesRepository
import com.lightningshop.jobtrack.data.repo.JobsRepository
import com.lightningshop.jobtrack.export.InvoiceExporter
import com.lightningshop.jobtrack.model.InvoiceStatus
import com.lightningshop.jobtrack.model.PaymentMethod
import com.lightningshop.jobtrack.model.centsFromAmount
import com.lightningshop.jobtrack.model.formatCents
import com.lightningshop.jobtrack.settings.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEditorScreen(
  prefs: AppPreferences,
  repo: InvoicesRepository,
  jobsRepo: JobsRepository,
  export: InvoiceExporter,
  invoiceId: String,
  onBack: () -> Unit,
) {
  val scope = rememberCoroutineScope()
  val ctx = LocalContext.current

  val snapshot by repo.observeSnapshot(invoiceId).collectAsState(initial = null)
  val currency by prefs.currencyCode.collectAsState(initial = "USD")

  if (snapshot == null) {
    Scaffold(
      topBar = {
        CenterAlignedTopAppBar(
          title = { Text("Invoice") },
          navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) } },
        )
      },
    ) { padding ->
      Column(Modifier.padding(padding).padding(16.dp)) {
        Text("Invoice not found.")
      }
    }
    return
  }

  val s = snapshot!!

  var showStatusMenu by remember { mutableStateOf(false) }
  var showEditBillTo by remember { mutableStateOf(false) }
  var showAddLine by remember { mutableStateOf(false) }
  var editLine: InvoiceLineEntity? by remember { mutableStateOf(null) }
  var showAddPayment by remember { mutableStateOf(false) }
  var confirmDelete by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            s.invoice.invoiceNumber,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") } },
        actions = {
          AssistChip(
            onClick = { showStatusMenu = true },
            label = { Text(s.invoice.status.name) },
            colors = assistChipColors(),
          )
          DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
            InvoiceStatus.entries.forEach { st ->
              DropdownMenuItem(
                text = { Text(st.name) },
                onClick = {
                  showStatusMenu = false
                  scope.launch { repo.upsertInvoice(s.invoice.copy(status = st, updatedAt = System.currentTimeMillis())) }
                },
              )
            }
          }
          IconButton(
            onClick = {
              scope.launch {
                val uri = export.exportPdf(invoiceId)
                openPdf(ctx, uri)
              }
            },
          ) { Icon(Icons.Outlined.PictureAsPdf, "Export PDF") }
          IconButton(
            onClick = {
              scope.launch {
                val uri = export.exportPdf(invoiceId)
                ctx.startActivity(Intent.createChooser(export.buildShareIntent(uri), "Share invoice"))
              }
            },
          ) { Icon(Icons.Outlined.IosShare, "Share") }
          IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, "Delete") }
        },
      )
    },
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        InvoiceTotalsCard(
          currency = currency,
          subtotalCents = s.subtotalCents,
          taxCents = s.taxCents,
          discountCents = s.discountCents,
          totalCents = s.totalCents,
          paidCents = s.paidCents,
          balanceCents = s.balanceCents,
        )
      }

      item {
        InvoiceMetaCard(
          invoice = s.invoice,
          onEditBillTo = { showEditBillTo = true },
        )
      }

      item {
        SectionHeader(
          title = "Line items",
          actionLabel = "Add",
          onAction = { showAddLine = true },
        )
      }

      items(s.lines, key = { it.id }) { line ->
        LineItemRow(
          line = line,
          currency = currency,
          onEdit = { editLine = line },
          onDelete = { scope.launch { repo.deleteLine(line.id) } },
        )
      }

      item {
        SectionHeader(
          title = "Payments",
          actionLabel = "Add",
          onAction = { showAddPayment = true },
        )
      }

      items(s.payments, key = { it.id }) { p ->
        PaymentRow(
          payment = p,
          currency = currency,
          onDelete = { scope.launch { repo.deletePayment(p.id); autoSyncStatus(repo, invoiceId) } },
        )
      }

      item {
        NotesCard(
          invoice = s.invoice,
          onSave = { newNotes ->
            scope.launch { repo.upsertInvoice(s.invoice.copy(notes = newNotes, updatedAt = System.currentTimeMillis())) }
          },
        )
      }

      item { Spacer(Modifier.padding(bottom = 20.dp)) }
    }
  }

  if (showEditBillTo) {
    EditBillToDialog(
      invoice = s.invoice,
      onDismiss = { showEditBillTo = false },
      onSave = { updated ->
        scope.launch {
          repo.upsertInvoice(updated.copy(updatedAt = System.currentTimeMillis()))
          showEditBillTo = false
        }
      },
    )
  }

  if (showAddLine) {
    EditLineDialog(
      title = "Add line item",
      initial = null,
      currency = currency,
      onDismiss = { showAddLine = false },
      onSave = { line ->
        scope.launch {
          val nextOrder = (s.lines.maxOfOrNull { it.sortOrder } ?: 0) + 1
          repo.upsertLine(line.copy(invoiceId = invoiceId, sortOrder = nextOrder))
          showAddLine = false
        }
      },
    )
  }

  if (editLine != null) {
    EditLineDialog(
      title = "Edit line item",
      initial = editLine,
      currency = currency,
      onDismiss = { editLine = null },
      onSave = { updated ->
        scope.launch {
          repo.upsertLine(updated)
          editLine = null
        }
      },
    )
  }

  if (showAddPayment) {
    AddPaymentDialog(
      currency = currency,
      onDismiss = { showAddPayment = false },
      onAdd = { cents, method, note ->
        scope.launch {
          repo.addPayment(
            PaymentEntity(
              invoiceId = invoiceId,
              amountCents = cents,
              method = method,
              date = LocalDate.now(),
              note = note,
            ),
          )
          autoSyncStatus(repo, invoiceId)
          showAddPayment = false
        }
      },
    )
  }

  if (confirmDelete) {
    AlertDialog(
      onDismissRequest = { confirmDelete = false },
      title = { Text("Delete invoice?") },
      text = { Text("This will permanently delete the invoice and its line items / payments.") },
      confirmButton = {
        TextButton(
          onClick = {
            scope.launch { repo.deleteInvoice(invoiceId); confirmDelete = false; onBack() }
          },
        ) { Text("Delete") }
      },
      dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
    )
  }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(title, style = MaterialTheme.typography.titleLarge)
    TextButton(onClick = onAction) {
      Icon(Icons.Outlined.Add, contentDescription = null)
      Spacer(Modifier.width(6.dp))
      Text(actionLabel)
    }
  }
}

@Composable
private fun InvoiceTotalsCard(
  currency: String,
  subtotalCents: Long,
  taxCents: Long,
  discountCents: Long,
  totalCents: Long,
  paidCents: Long,
  balanceCents: Long,
) {
  androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Subtotal")
        Text(formatCents(subtotalCents, currency))
      }
      if (taxCents > 0) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Tax")
          Text(formatCents(taxCents, currency))
        }
      }
      if (discountCents > 0) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Discount")
          Text("-" + formatCents(discountCents, currency))
        }
      }
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Total", style = MaterialTheme.typography.titleMedium)
        Text(formatCents(totalCents, currency), style = MaterialTheme.typography.titleMedium)
      }
      if (paidCents > 0) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Paid")
          Text("-" + formatCents(paidCents, currency))
        }
      }
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Balance", style = MaterialTheme.typography.titleMedium)
        Text(formatCents(balanceCents, currency), style = MaterialTheme.typography.titleMedium)
      }
    }
  }
}

@Composable
private fun InvoiceMetaCard(invoice: InvoiceEntity, onEditBillTo: () -> Unit) {
  val df = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
  androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Bill To", style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onEditBillTo) { Icon(Icons.Outlined.Edit, contentDescription = "Edit") }
      }
      Text(invoice.billToName.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)
      if (invoice.billToAddress.isNotBlank()) Text(invoice.billToAddress, style = MaterialTheme.typography.bodySmall)
      val contact = listOf(invoice.billToPhone, invoice.billToEmail).filter { it.isNotBlank() }.joinToString(" • ")
      if (contact.isNotBlank()) Text(contact, style = MaterialTheme.typography.bodySmall)

      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Issue")
        Text(invoice.issueDate.format(df))
      }
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Due")
        Text(invoice.dueDate?.format(df) ?: "—")
      }
    }
  }
}

@Composable
private fun LineItemRow(
  line: InvoiceLineEntity,
  currency: String,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val totalCents = ((line.quantity * (line.unitPriceCents / 100.0)) * 100).toLong()
  androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(line.description, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(formatCents(totalCents, currency), style = MaterialTheme.typography.titleMedium)
      }
      Text("${line.quantity} ${line.unitLabel} @ ${formatCents(line.unitPriceCents, currency)}", style = MaterialTheme.typography.bodySmall)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onEdit) { Text("Edit") }
        TextButton(onClick = onDelete) { Text("Delete") }
      }
    }
  }
}

@Composable
private fun PaymentRow(payment: PaymentEntity, currency: String, onDelete: () -> Unit) {
  androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(payment.method.name.replace("_", " "), style = MaterialTheme.typography.titleMedium)
        Text(formatCents(payment.amountCents, currency), style = MaterialTheme.typography.titleMedium)
      }
      if (payment.note.isNotBlank()) Text(payment.note, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onDelete) { Text("Delete") }
      }
    }
  }
}

@Composable
private fun NotesCard(invoice: InvoiceEntity, onSave: (String) -> Unit) {
  var notes by remember(invoice.notes) { mutableStateOf(invoice.notes) }
  androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text("Notes", style = MaterialTheme.typography.titleLarge)
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
      )
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { onSave(notes) }) { Text("Save notes") }
      }
    }
  }
}

@Composable
private fun EditBillToDialog(
  invoice: InvoiceEntity,
  onDismiss: () -> Unit,
  onSave: (InvoiceEntity) -> Unit,
) {
  var name by remember(invoice.billToName) { mutableStateOf(invoice.billToName) }
  var addr by remember(invoice.billToAddress) { mutableStateOf(invoice.billToAddress) }
  var phone by remember(invoice.billToPhone) { mutableStateOf(invoice.billToPhone) }
  var email by remember(invoice.billToEmail) { mutableStateOf(invoice.billToEmail) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Bill To") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = addr, onValueChange = { addr = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
      }
    },
    confirmButton = {
      TextButton(onClick = {
        onSave(invoice.copy(billToName = name.trim(), billToAddress = addr.trim(), billToPhone = phone.trim(), billToEmail = email.trim()))
      }) { Text("Save") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Composable
private fun EditLineDialog(
  title: String,
  initial: InvoiceLineEntity?,
  currency: String,
  onDismiss: () -> Unit,
  onSave: (InvoiceLineEntity) -> Unit,
) {
  var desc by remember(initial?.description) { mutableStateOf(initial?.description ?: "") }
  var qty by remember(initial?.quantity) { mutableStateOf((initial?.quantity ?: 1.0).toString()) }
  var unit by remember(initial?.unitLabel) { mutableStateOf(initial?.unitLabel ?: "each") }
  var price by remember(initial?.unitPriceCents) { mutableStateOf(((initial?.unitPriceCents ?: 0) / 100.0).toString()) }
  var taxable by remember(initial?.taxable) { mutableStateOf(initial?.taxable ?: true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
          OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(
          value = price,
          onValueChange = { price = it },
          label = { Text("Unit price ($currency)") },
          modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Taxable")
          androidx.compose.material3.Switch(checked = taxable, onCheckedChange = { taxable = it })
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          val q = qty.trim().toDoubleOrNull() ?: 1.0
          val cents = centsFromAmount(price)
          val base = initial ?: InvoiceLineEntity(invoiceId = "", description = "", unitPriceCents = 0)
          onSave(base.copy(description = desc.trim(), quantity = q, unitLabel = unit.trim().ifBlank { "each" }, unitPriceCents = cents, taxable = taxable))
        },
      ) { Text("Save") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

@Composable
private fun AddPaymentDialog(
  currency: String,
  onDismiss: () -> Unit,
  onAdd: (amountCents: Long, method: PaymentMethod, note: String) -> Unit,
) {
  var amount by remember { mutableStateOf("") }
  var note by remember { mutableStateOf("") }
  var method by remember { mutableStateOf(PaymentMethod.OTHER) }
  var menu by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add payment") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount ($currency)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Method")
          TextButton(onClick = { menu = true }) { Text(method.name.replace("_", " ")) }
          DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            PaymentMethod.entries.forEach { m ->
              DropdownMenuItem(text = { Text(m.name.replace("_", " ")) }, onClick = { method = m; menu = false })
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = {
        val cents = centsFromAmount(amount)
        if (cents > 0) onAdd(cents, method, note.trim())
      }) { Text("Add") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

private suspend fun autoSyncStatus(repo: InvoicesRepository, invoiceId: String) {
  val snap = repo.observeSnapshot(invoiceId).first() ?: return
  val inv = snap.invoice
  val next = when {
    inv.status == InvoiceStatus.VOID -> InvoiceStatus.VOID
    snap.balanceCents == 0L && snap.totalCents > 0L -> InvoiceStatus.PAID
    snap.paidCents > 0L -> InvoiceStatus.PARTIAL
    else -> inv.status
  }
  if (next != inv.status) {
    repo.upsertInvoice(inv.copy(status = next, updatedAt = System.currentTimeMillis()))
  }
}

private fun openPdf(context: Context, uri: android.net.Uri) {
  val intent = Intent(Intent.ACTION_VIEW).apply {
    setDataAndType(uri, "application/pdf")
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }
  context.startActivity(intent)
}
