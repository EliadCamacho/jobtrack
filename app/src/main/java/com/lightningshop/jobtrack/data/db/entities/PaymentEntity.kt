package com.lightningshop.jobtrack.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lightningshop.jobtrack.model.PaymentMethod
import java.time.LocalDate
import java.util.UUID

@Entity(
  tableName = "payments",
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
    Index(value = ["date"]),
  ],
)
data class PaymentEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val invoiceId: String,
  val amountCents: Long,
  val method: PaymentMethod = PaymentMethod.OTHER,
  val date: LocalDate = LocalDate.now(),
  val note: String = "",
  val createdAt: Long = System.currentTimeMillis(),
)
