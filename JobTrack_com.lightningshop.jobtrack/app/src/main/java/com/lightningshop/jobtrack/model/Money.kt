package com.lightningshop.jobtrack.model

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToLong

fun formatCents(cents: Long, currencyCode: String): String {
  val fmt = NumberFormat.getCurrencyInstance(Locale.US)
  runCatching { fmt.currency = Currency.getInstance(currencyCode) }
  return fmt.format(cents / 100.0)
}

/**
 * Accepts user input like "12.34", "$12.34", "1,234.00" and returns cents.
 */
fun centsFromAmount(amount: String): Long {
  val cleaned = amount.trim()
    .replace("$", "")
    .replace(",", "")
  val d = cleaned.toDoubleOrNull() ?: 0.0
  return (d * 100.0).roundToLong()
}
