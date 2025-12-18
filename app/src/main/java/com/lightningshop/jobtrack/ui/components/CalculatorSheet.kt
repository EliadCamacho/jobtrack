package com.lightningshop.jobtrack.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.model.formatCents
import com.lightningshop.jobtrack.settings.AppPreferences
import kotlinx.coroutines.launch

@Composable
fun CalculatorSheet(
  prefs: AppPreferences,
  initialRate: Double,
  initialMultiplier: Double,
  currencyCode: String,
) {
  val scope = rememberCoroutineScope()

  var sqft by remember { mutableStateOf("1600") }
  var rate by remember { mutableStateOf(initialRate.toString()) }
  var mult by remember { mutableStateOf(initialMultiplier.toString()) }

  fun num(s: String): Double = s.trim().replace(",", "").toDoubleOrNull() ?: 0.0

  val total = num(sqft) * num(rate) * num(mult)
  val cents = (total * 100.0).toLong()

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text("Quick Job Calculator", style = MaterialTheme.typography.titleLarge)

    OutlinedTextField(
      value = sqft,
      onValueChange = { sqft = it },
      label = { Text("Square feet") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedTextField(
        value = rate,
        onValueChange = { rate = it },
        label = { Text("Rate ($/sqft)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.weight(1f),
      )
      OutlinedTextField(
        value = mult,
        onValueChange = { mult = it },
        label = { Text("Multiplier") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.weight(1f),
      )
    }

    Divider()

    Text("Total: ${formatCents(cents, currencyCode)}", style = MaterialTheme.typography.headlineSmall)

    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Button(
        onClick = {
          scope.launch { prefs.setCalculatorDefaults(num(rate), num(mult)) }
        },
        modifier = Modifier.weight(1f),
      ) { Text("Save defaults") }

      TextButton(
        onClick = {
          sqft = ""
          rate = initialRate.toString()
          mult = initialMultiplier.toString()
        },
        modifier = Modifier.weight(1f),
      ) { Text("Reset") }
    }

    Spacer(Modifier.height(12.dp))
  }
}
