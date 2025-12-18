package com.lightningshop.jobtrack.ui.screens.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.model.ExpenseType
import com.lightningshop.jobtrack.model.centsFromAmount

@Composable
fun AddExpenseDialog(
  currency: String,
  onDismiss: () -> Unit,
  onAdd: (type: ExpenseType, vendor: String, description: String, amountCents: Long, category: String) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  var type by remember { mutableStateOf(ExpenseType.MATERIAL) }
  var vendor by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var amount by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add expense") },
    text = {
      Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = { expanded = !expanded },
        ) {
          OutlinedTextField(
            value = type.name.replace("_", " "),
            onValueChange = {},
            readOnly = true,
            label = { Text("Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(),
          )
          ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
          ) {
            ExpenseType.entries.forEach { t ->
              androidx.compose.material3.DropdownMenuItem(
                text = { Text(t.name.replace("_", " ")) },
                onClick = {
                  type = t
                  expanded = false
                },
              )
            }
          }
        }

        OutlinedTextField(
          value = vendor,
          onValueChange = { vendor = it },
          label = { Text("Vendor") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Category (optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
          value = amount,
          onValueChange = { amount = it },
          label = { Text("Amount ($currency)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (description.trim().isNotEmpty()) {
            onAdd(
              type,
              vendor.trim(),
              description.trim(),
              centsFromAmount(amount),
              category.trim(),
            )
          }
        },
      ) { Text("Add") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}
