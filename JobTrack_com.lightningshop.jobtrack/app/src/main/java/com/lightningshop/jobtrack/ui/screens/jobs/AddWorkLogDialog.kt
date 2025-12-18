package com.lightningshop.jobtrack.ui.screens.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.model.centsFromAmount

@Composable
fun AddWorkLogDialog(
  currency: String,
  onDismiss: () -> Unit,
  onAdd: (worker: String, hours: Double, rateCentsPerHour: Long, note: String) -> Unit,
) {
  var worker by remember { mutableStateOf("") }
  var hours by remember { mutableStateOf("8") }
  var rate by remember { mutableStateOf("0") }
  var note by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add work log") },
    text = {
      Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = worker,
          onValueChange = { worker = it },
          label = { Text("Worker name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = hours,
          onValueChange = { hours = it },
          label = { Text("Hours") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = rate,
          onValueChange = { rate = it },
          label = { Text("Rate per hour ($currency)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("Note (optional)") },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          val h = hours.trim().toDoubleOrNull() ?: 0.0
          val centsPerHour = centsFromAmount(rate)
          if (worker.trim().isNotEmpty() && h > 0) onAdd(worker.trim(), h, centsPerHour, note.trim())
        },
      ) { Text("Add") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
