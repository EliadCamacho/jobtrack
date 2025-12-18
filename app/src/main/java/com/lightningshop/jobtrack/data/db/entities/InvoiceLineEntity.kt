package com.lightningshop.jobtrack.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "invoice_lines",
  foreignKeys = [
    ForeignKey(
      entity = InvoiceEntity::class,
      parentColumns = ["id"],
      childColumns = ["invoiceId"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [
    Index(value = ["invoiceId"]),
    Index(value = ["sortOrder"]),
  ],
)
data class InvoiceLineEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val invoiceId: String,
  val description: String,
  val quantity: Double = 1.0,
  val unitLabel: String = "each",
  val unitPriceCents: Long,
  val taxable: Boolean = true,
  val sortOrder: Int = 0,
  val createdAt: Long = System.currentTimeMillis(),
)
