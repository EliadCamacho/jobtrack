package com.lightningshop.jobtrack.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lightningshop.jobtrack.model.InvoiceStatus
import java.time.LocalDate
import java.util.UUID

@Entity(
  tableName = "invoices",
  foreignKeys = [
    ForeignKey(
      entity = JobEntity::class,
      parentColumns = ["id"],
      childColumns = ["jobId"],
      onDelete = ForeignKey.SET_NULL,
    ),
  ],
  indices = [
    Index(value = ["jobId"]),
    Index(value = ["issueDate"]),
    Index(value = ["status"]),
    Index(value = ["invoiceNumber"], unique = true),
  ],
)
data class InvoiceEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val invoiceNumber: String,
  val jobId: String? = null,
  val status: InvoiceStatus = InvoiceStatus.DRAFT,

  val billToName: String,
  val billToAddress: String = "",
  val billToEmail: String = "",
  val billToPhone: String = "",

  val issueDate: LocalDate = LocalDate.now(),
  val dueDate: LocalDate? = null,

  val notes: String = "",
  val taxRateBps: Int = 0, // basis points: 825 = 8.25%
  val discountCents: Long = 0,

  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)
