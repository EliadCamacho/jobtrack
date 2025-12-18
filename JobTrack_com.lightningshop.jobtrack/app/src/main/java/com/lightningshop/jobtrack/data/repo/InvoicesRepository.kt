package com.lightningshop.jobtrack.data.repo

import com.lightningshop.jobtrack.data.db.dao.InvoiceDao
import com.lightningshop.jobtrack.data.db.dao.InvoiceLineDao
import com.lightningshop.jobtrack.data.db.dao.PaymentDao
import com.lightningshop.jobtrack.data.db.entities.InvoiceEntity
import com.lightningshop.jobtrack.data.db.entities.InvoiceLineEntity
import com.lightningshop.jobtrack.data.db.entities.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class InvoiceSnapshot(
  val invoice: InvoiceEntity,
  val lines: List<InvoiceLineEntity>,
  val payments: List<PaymentEntity>,
  val subtotalCents: Long,
  val taxCents: Long,
  val discountCents: Long,
  val totalCents: Long,
  val paidCents: Long,
  val balanceCents: Long,
)

class InvoicesRepository(
  private val invoiceDao: InvoiceDao,
  private val lineDao: InvoiceLineDao,
  private val paymentDao: PaymentDao,
) {
  fun observeInvoices(): Flow<List<InvoiceEntity>> = invoiceDao.observeAll()
  fun observeInvoice(id: String): Flow<InvoiceEntity?> = invoiceDao.observeById(id)
  fun observeLines(invoiceId: String): Flow<List<InvoiceLineEntity>> = lineDao.observeForInvoice(invoiceId)
  fun observePayments(invoiceId: String): Flow<List<PaymentEntity>> = paymentDao.observeForInvoice(invoiceId)

  fun observeSnapshot(invoiceId: String): Flow<InvoiceSnapshot?> =
    combine(
      invoiceDao.observeById(invoiceId),
      lineDao.observeForInvoice(invoiceId),
      paymentDao.observeForInvoice(invoiceId),
    ) { invoice, lines, payments ->
      invoice ?: return@combine null

      val subtotal = lines.sumOf { line ->
        val qty = line.quantity
        ((qty * (line.unitPriceCents / 100.0)) * 100).toLong()
      }

      val taxableSubtotal = lines.filter { it.taxable }.sumOf { line ->
        ((line.quantity * (line.unitPriceCents / 100.0)) * 100).toLong()
      }

      val tax = (taxableSubtotal * invoice.taxRateBps / 10_000.0).toLong()
      val discount = invoice.discountCents
      val total = (subtotal + tax - discount).coerceAtLeast(0)

      val paid = payments.sumOf { it.amountCents }
      val balance = (total - paid).coerceAtLeast(0)

      InvoiceSnapshot(
        invoice = invoice,
        lines = lines.sortedWith(compareBy<InvoiceLineEntity> { it.sortOrder }.thenBy { it.createdAt }),
        payments = payments,
        subtotalCents = subtotal,
        taxCents = tax,
        discountCents = discount,
        totalCents = total,
        paidCents = paid,
        balanceCents = balance,
      )
    }

  suspend fun upsertInvoice(invoice: InvoiceEntity) = invoiceDao.upsert(invoice)
  suspend fun deleteInvoice(id: String) = invoiceDao.delete(id)
  suspend fun getInvoice(id: String): InvoiceEntity? = invoiceDao.getById(id)

  suspend fun upsertLine(line: InvoiceLineEntity) = lineDao.upsert(line)
  suspend fun deleteLine(id: String) = lineDao.delete(id)

  suspend fun addPayment(payment: PaymentEntity) = paymentDao.insert(payment)
  suspend fun deletePayment(id: String) = paymentDao.delete(id)
}
