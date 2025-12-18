package com.lightningshop.jobtrack.export

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.lightningshop.jobtrack.data.db.entities.InvoiceEntity
import com.lightningshop.jobtrack.data.db.entities.InvoiceLineEntity
import com.lightningshop.jobtrack.data.repo.InvoicesRepository
import com.lightningshop.jobtrack.data.repo.InvoiceSnapshot
import com.lightningshop.jobtrack.data.repo.JobsRepository
import com.lightningshop.jobtrack.model.InvoiceStatus
import com.lightningshop.jobtrack.model.formatCents
import com.lightningshop.jobtrack.settings.AppPreferences
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class InvoiceExporter(
  private val context: Context,
  private val prefs: AppPreferences,
  private val invoices: InvoicesRepository,
  private val jobs: JobsRepository,
) {

  suspend fun createDraftInvoiceForJob(jobId: String?): String {
    val branding = prefs.invoiceBranding.first()
    val number = buildInvoiceNumber()
    val invoice = InvoiceEntity(
      invoiceNumber = number,
      jobId = jobId,
      status = InvoiceStatus.DRAFT,
      billToName = jobId?.let { jobs.getJob(it)?.customer }?.takeIf { it.isNotBlank() } ?: "",
      billToAddress = jobId?.let { jobs.getJob(it)?.address } ?: "",
      taxRateBps = branding.defaultTaxBps,
      notes = branding.defaultNotes,
    )
    invoices.upsertInvoice(invoice)

    // Seed with a single line so the editor feels "alive"
    invoices.upsertLine(
      InvoiceLineEntity(
        invoiceId = invoice.id,
        description = "Labor",
        quantity = 1.0,
        unitLabel = "job",
        unitPriceCents = 0,
        taxable = true,
        sortOrder = 0,
      ),
    )

    return invoice.id
  }

  suspend fun exportPdf(invoiceId: String): Uri {
    val snapshot = invoices.observeSnapshot(invoiceId).first() ?: error("Invoice not found")
    val branding = prefs.invoiceBranding.first()
    val currency = prefs.currencyCode.first()

    val file = File(context.cacheDir, "invoice-${snapshot.invoice.invoiceNumber}.pdf")
    FileOutputStream(file).use { fos ->
      val doc = PdfDocument()
      val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create() // US Letter @72dpi
      val page = doc.startPage(pageInfo)

      val canvas = page.canvas
      val paint = Paint(Paint.ANTI_ALIAS_FLAG)
      val bold = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
      val mono = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.MONOSPACE }

      val margin = 36f
      var y = margin

      bold.textSize = 20f
      canvas.drawText(branding.companyName, margin, y, bold)
      y += 20f

      paint.textSize = 10.5f
      if (branding.companyAddress.isNotBlank()) {
        canvas.drawText(branding.companyAddress, margin, y, paint); y += 14f
      }
      val contact = listOf(branding.companyPhone, branding.companyEmail).filter { it.isNotBlank() }.joinToString(" • ")
      if (contact.isNotBlank()) { canvas.drawText(contact, margin, y, paint); y += 14f }

      y += 10f

      bold.textSize = 16f
      canvas.drawText("INVOICE", margin, y, bold)

      val rightX = pageInfo.pageWidth - margin
      paint.textAlign = Paint.Align.RIGHT
      paint.textSize = 11.5f
      canvas.drawText(snapshot.invoice.invoiceNumber, rightX, y, paint)
      y += 18f

      paint.textSize = 10.5f
      val df = DateTimeFormatter.ofPattern("MMM d, yyyy")
      val issue = snapshot.invoice.issueDate.format(df)
      val due = snapshot.invoice.dueDate?.format(df) ?: "—"
      canvas.drawText("Issue: $issue", rightX, y, paint); y += 14f
      canvas.drawText("Due:   $due", rightX, y, paint); y += 14f

      paint.textAlign = Paint.Align.LEFT
      y += 8f

      // Bill To
      bold.textSize = 11.5f
      canvas.drawText("Bill To", margin, y, bold); y += 14f
      paint.textSize = 10.5f
      canvas.drawText(snapshot.invoice.billToName.ifBlank { "—" }, margin, y, paint); y += 14f
      if (snapshot.invoice.billToAddress.isNotBlank()) { canvas.drawText(snapshot.invoice.billToAddress, margin, y, paint); y += 14f }
      val billContact = listOf(snapshot.invoice.billToPhone, snapshot.invoice.billToEmail).filter { it.isNotBlank() }.joinToString(" • ")
      if (billContact.isNotBlank()) { canvas.drawText(billContact, margin, y, paint); y += 14f }

      y += 14f

      // Table header
      val colDesc = margin
      val colQty = 380f
      val colUnit = 430f
      val colAmount = rightX

      bold.textSize = 10.5f
      canvas.drawText("Description", colDesc, y, bold)
      canvas.drawText("Qty", colQty, y, bold)
      canvas.drawText("Unit", colUnit, y, bold)
      paint.textAlign = Paint.Align.RIGHT
      canvas.drawText("Amount", colAmount, y, bold)
      paint.textAlign = Paint.Align.LEFT
      y += 8f
      canvas.drawLine(margin, y, rightX, y, paint)
      y += 14f

      // Lines
      paint.textSize = 10.5f
      snapshot.lines.forEach { line ->
        if (y > 610f) {
          // (simple 1-page limit for now)
          return@forEach
        }
        canvas.drawText(trimTo(line.description, 45), colDesc, y, paint)
        canvas.drawText(formatQty(line.quantity), colQty, y, paint)
        canvas.drawText(trimTo(line.unitLabel, 8), colUnit, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(formatCents(lineTotalCents(line), currency), colAmount, y, paint)
        paint.textAlign = Paint.Align.LEFT
        y += 16f
      }

      y += 8f
      canvas.drawLine(margin, y, rightX, y, paint)
      y += 16f

      // Totals
      val labelX = 430f
      val valueX = rightX
      fun row(label: String, value: String) {
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(label, labelX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, valueX, y, paint)
        y += 14f
      }

      row("Subtotal", formatCents(snapshot.subtotalCents, currency))
      if (snapshot.taxCents > 0) row("Tax", formatCents(snapshot.taxCents, currency))
      if (snapshot.discountCents > 0) row("Discount", "-${formatCents(snapshot.discountCents, currency)}")

      bold.textSize = 11.5f
      paint.textSize = 11.5f
      paint.textAlign = Paint.Align.LEFT
      canvas.drawText("Total", labelX, y, bold)
      paint.textAlign = Paint.Align.RIGHT
      canvas.drawText(formatCents(snapshot.totalCents, currency), valueX, y, bold)
      y += 18f

      paint.textSize = 10.5f
      if (snapshot.paidCents > 0) row("Paid", "-${formatCents(snapshot.paidCents, currency)}")
      row("Balance", formatCents(snapshot.balanceCents, currency))

      y += 14f
      paint.textAlign = Paint.Align.LEFT
      if (snapshot.invoice.notes.isNotBlank()) {
        bold.textSize = 11.5f
        canvas.drawText("Notes", margin, y, bold); y += 14f
        paint.textSize = 10.5f
        val wrapped = wrap(snapshot.invoice.notes, 80)
        wrapped.forEach { line ->
          if (y > 740f) return@forEach
          canvas.drawText(line, margin, y, paint); y += 14f
        }
      }

      doc.finishPage(page)
      doc.writeTo(fos)
      doc.close()
    }

    return FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
  }

  fun buildShareIntent(pdfUri: Uri): Intent =
    Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"
      putExtra(Intent.EXTRA_STREAM, pdfUri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

  private fun buildInvoiceNumber(): String {
    val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
    val short = UUID.randomUUID().toString().take(6).uppercase()
    return "INV-$date-$short"
  }

  private fun lineTotalCents(line: InvoiceLineEntity): Long =
    ((line.quantity * (line.unitPriceCents / 100.0)) * 100).toLong()

  private fun formatQty(q: Double): String =
    if (q % 1.0 == 0.0) q.toInt().toString() else String.format("%.2f", q)

  private fun trimTo(s: String, max: Int): String = if (s.length <= max) s else s.take(max - 1) + "…"

  private fun wrap(text: String, maxLine: Int): List<String> {
    val words = text.split(Regex("\s+"))
    val out = mutableListOf<String>()
    var cur = ""
    for (w in words) {
      if (cur.isEmpty()) cur = w
      else if ((cur.length + 1 + w.length) <= maxLine) cur += " $w"
      else { out += cur; cur = w }
    }
    if (cur.isNotEmpty()) out += cur
    return out
  }
}
