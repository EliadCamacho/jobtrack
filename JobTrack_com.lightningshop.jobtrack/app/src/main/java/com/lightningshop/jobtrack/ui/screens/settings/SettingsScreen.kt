package com.lightningshop.jobtrack.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.settings.AppPreferences
import com.lightningshop.jobtrack.settings.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(prefs: AppPreferences) {
  val scope = rememberCoroutineScope()

  val theme by prefs.themeState.collectAsState(initial = prefs.defaultThemeState())
  val branding by prefs.invoiceBranding.collectAsState(
    initial = com.lightningshop.jobtrack.settings.InvoiceBranding(
      companyName = "Lightning Shop LLC",
      companyAddress = "Homestead, FL",
      companyPhone = "",
      companyEmail = "",
      accentColorArgb = 0xFF5BB6FF,
      defaultTaxBps = 0,
      defaultNotes = "Thank you for your business.",
    ),
  )
  val currency by prefs.currencyCode.collectAsState(initial = "USD")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text("Settings", style = MaterialTheme.typography.headlineLarge)

    // Theme
    Card(Modifier.fillMaxWidth()) {
      Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Theme", style = MaterialTheme.typography.titleLarge)

        ThemeModePicker(
          value = theme.mode,
          onChange = { scope.launch { prefs.setThemeMode(it) } },
        )

        SettingSwitch(
          label = "Dynamic color (Android 12+)",
          checked = theme.dynamicColor,
          onCheckedChange = { scope.launch { prefs.setDynamicColor(it) } },
        )

        SettingSwitch(
          label = "Bottom nav labels",
          checked = theme.bottomLabels,
          onCheckedChange = { scope.launch { prefs.setBottomLabels(it) } },
        )

        Text("Corner radius: ${theme.cornerRadiusDp.toInt()}dp", style = MaterialTheme.typography.bodyMedium)
        Slider(
          value = theme.cornerRadiusDp,
          onValueChange = { scope.launch { prefs.setCornerRadiusDp(it) } },
          valueRange = 8f..30f,
        )

        Text("Typography scale: ${"%.2f".format(theme.typographyScale)}x", style = MaterialTheme.typography.bodyMedium)
        Slider(
          value = theme.typographyScale,
          onValueChange = { scope.launch { prefs.setTypographyScale(it) } },
          valueRange = 0.9f..1.15f,
        )
      }
    }

    // Currency
    Card(Modifier.fillMaxWidth()) {
      Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Currency", style = MaterialTheme.typography.titleLarge)
        CurrencyPicker(value = currency, onChange = { scope.launch { prefs.setCurrency(it) } })
      }
    }

    // Invoice Branding
    Card(Modifier.fillMaxWidth()) {
      Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Invoice branding", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
          value = branding.companyName,
          onValueChange = { v -> scope.launch { prefs.setBranding { it.copy(companyName = v) } } },
          label = { Text("Company name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        OutlinedTextField(
          value = branding.companyAddress,
          onValueChange = { v -> scope.launch { prefs.setBranding { it.copy(companyAddress = v) } } },
          label = { Text("Company address") },
          modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
          value = branding.companyPhone,
          onValueChange = { v -> scope.launch { prefs.setBranding { it.copy(companyPhone = v) } } },
          label = { Text("Company phone") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        OutlinedTextField(
          value = branding.companyEmail,
          onValueChange = { v -> scope.launch { prefs.setBranding { it.copy(companyEmail = v) } } },
          label = { Text("Company email") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        OutlinedTextField(
          value = branding.defaultTaxBps.toString(),
          onValueChange = { v ->
            val bps = v.filter { it.isDigit() }.toIntOrNull() ?: 0
            scope.launch { prefs.setBranding { it.copy(defaultTaxBps = bps.coerceIn(0, 2_000)) } }
          },
          label = { Text("Default tax (bps) - e.g. 700 = 7%") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )
        OutlinedTextField(
          value = branding.defaultNotes,
          onValueChange = { v -> scope.launch { prefs.setBranding { it.copy(defaultNotes = v) } } },
          label = { Text("Default invoice notes") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
        )
      }
    }
  }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label, style = MaterialTheme.typography.bodyMedium)
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

@Composable
private fun ThemeModePicker(value: ThemeMode, onChange: (ThemeMode) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text("Mode", style = MaterialTheme.typography.bodyMedium)
    TextButton(onClick = { expanded = true }) { Text(value.name) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      ThemeMode.entries.forEach { mode ->
        DropdownMenuItem(
          text = { Text(mode.name) },
          onClick = {
            expanded = false
            onChange(mode)
          },
        )
      }
    }
  }
}

@Composable
private fun CurrencyPicker(value: String, onChange: (String) -> Unit) {
  val options = listOf("USD", "EUR", "GBP", "CAD", "MXN")
  var expanded by remember { mutableStateOf(false) }
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text("Currency", style = MaterialTheme.typography.bodyMedium)
    TextButton(onClick = { expanded = true }) { Text(value) }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      options.forEach { code ->
        DropdownMenuItem(
          text = { Text(code) },
          onClick = { expanded = false; onChange(code) },
        )
      }
    }
  }
}
