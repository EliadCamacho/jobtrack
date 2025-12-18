package com.lightningshop.jobtrack.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jobtrack_prefs")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class ThemeState(
  val mode: ThemeMode,
  val dynamicColor: Boolean,
  val seedColorArgb: Long,
  val cornerRadiusDp: Float,
  val typographyScale: Float,
  val bottomLabels: Boolean,
)

data class InvoiceBranding(
  val companyName: String,
  val companyAddress: String,
  val companyPhone: String,
  val companyEmail: String,
  val accentColorArgb: Long,
  val defaultTaxBps: Int,
  val defaultNotes: String,
)

data class CalculatorPrefs(
  val defaultRatePerSqft: Double,
  val defaultMultiplier: Double,
  val cornerX: Float,
  val cornerY: Float,
)

class AppPreferences(private val context: Context) {

  private object Keys {
    val themeMode = stringPreferencesKey("theme_mode")
    val dynamicColor = booleanPreferencesKey("dynamic_color")
    val seedColorArgb = longPreferencesKey("seed_color_argb")
    val cornerRadiusDp = floatPreferencesKey("corner_radius_dp")
    val typographyScale = floatPreferencesKey("typography_scale")
    val bottomLabels = booleanPreferencesKey("bottom_labels")

    val currencyCode = stringPreferencesKey("currency_code")

    val companyName = stringPreferencesKey("company_name")
    val companyAddress = stringPreferencesKey("company_address")
    val companyPhone = stringPreferencesKey("company_phone")
    val companyEmail = stringPreferencesKey("company_email")
    val invoiceAccentArgb = longPreferencesKey("invoice_accent_argb")
    val invoiceDefaultTaxBps = intPreferencesKey("invoice_default_tax_bps")
    val invoiceDefaultNotes = stringPreferencesKey("invoice_default_notes")

    val calcRatePerSqft = doublePreferencesKey("calc_rate_per_sqft")
    val calcMultiplier = doublePreferencesKey("calc_multiplier")
    val calcCornerX = floatPreferencesKey("calc_corner_x")
    val calcCornerY = floatPreferencesKey("calc_corner_y")
  }

  val currencyCode: Flow<String> = context.dataStore.data.map { it[Keys.currencyCode] ?: "USD" }

  fun defaultThemeState() = ThemeState(
    mode = ThemeMode.SYSTEM,
    dynamicColor = true,
    seedColorArgb = 0xFF5BB6FF,
    cornerRadiusDp = 18f,
    typographyScale = 1.0f,
    bottomLabels = true,
  )

  val themeState: Flow<ThemeState> =
    context.dataStore.data.map { p ->
      ThemeState(
        mode = ThemeMode.valueOf(p[Keys.themeMode] ?: ThemeMode.SYSTEM.name),
        dynamicColor = p[Keys.dynamicColor] ?: true,
        seedColorArgb = p[Keys.seedColorArgb] ?: 0xFF5BB6FF,
        cornerRadiusDp = p[Keys.cornerRadiusDp] ?: 18f,
        typographyScale = (p[Keys.typographyScale] ?: 1.0f).coerceIn(0.85f, 1.25f),
        bottomLabels = p[Keys.bottomLabels] ?: true,
      )
    }

  val invoiceBranding: Flow<InvoiceBranding> =
    context.dataStore.data.map { p ->
      InvoiceBranding(
        companyName = p[Keys.companyName] ?: "Lightning Shop LLC",
        companyAddress = p[Keys.companyAddress] ?: "Homestead, FL",
        companyPhone = p[Keys.companyPhone] ?: "",
        companyEmail = p[Keys.companyEmail] ?: "",
        accentColorArgb = p[Keys.invoiceAccentArgb] ?: 0xFF5BB6FF,
        defaultTaxBps = p[Keys.invoiceDefaultTaxBps] ?: 0,
        defaultNotes = p[Keys.invoiceDefaultNotes] ?: "Thank you for your business.",
      )
    }

  val calculatorPrefs: Flow<CalculatorPrefs> =
    context.dataStore.data.map { p ->
      CalculatorPrefs(
        defaultRatePerSqft = p[Keys.calcRatePerSqft] ?: 0.22,
        defaultMultiplier = p[Keys.calcMultiplier] ?: 1.0,
        cornerX = p[Keys.calcCornerX] ?: 0.85f,
        cornerY = p[Keys.calcCornerY] ?: 0.65f,
      )
    }

  suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.themeMode] = mode.name }
  suspend fun setDynamicColor(enabled: Boolean) = edit { it[Keys.dynamicColor] = enabled }
  suspend fun setSeedColor(argb: Long) = edit { it[Keys.seedColorArgb] = argb }
  suspend fun setCornerRadiusDp(dp: Float) = edit { it[Keys.cornerRadiusDp] = dp }
  suspend fun setTypographyScale(scale: Float) = edit { it[Keys.typographyScale] = scale }
  suspend fun setBottomLabels(enabled: Boolean) = edit { it[Keys.bottomLabels] = enabled }

  suspend fun setCurrency(code: String) = edit { it[Keys.currencyCode] = code }

  
  suspend fun setBranding(update: (InvoiceBranding) -> InvoiceBranding) {
    val current = invoiceBranding.first()
    val next = update(current)
    edit {
      it[Keys.companyName] = next.companyName
      it[Keys.companyAddress] = next.companyAddress
      it[Keys.companyPhone] = next.companyPhone
      it[Keys.companyEmail] = next.companyEmail
      it[Keys.invoiceAccentArgb] = next.accentColorArgb
      it[Keys.invoiceDefaultTaxBps] = next.defaultTaxBps
      it[Keys.invoiceDefaultNotes] = next.defaultNotes
    }
  }

  suspend fun setCalculatorDefaults(ratePerSqft: Double, multiplier: Double) = edit {
    it[Keys.calcRatePerSqft] = ratePerSqft
    it[Keys.calcMultiplier] = multiplier
  }

  suspend fun setCalculatorCorner(x: Float, y: Float) = edit {
    it[Keys.calcCornerX] = x.coerceIn(0f, 1f)
    it[Keys.calcCornerY] = y.coerceIn(0f, 1f)
  }

  private suspend fun edit(block: suspend (Preferences.MutablePreferences) -> Unit) {
    context.dataStore.edit { prefs -> block(prefs) }
  }
}
