package com.lightningshop.jobtrack.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lightningshop.jobtrack.model.ExpenseType
import java.time.LocalDate
import java.util.UUID

@Entity(
  tableName = "expenses",
  foreignKeys = [
    ForeignKey(
      entity = JobEntity::class,
      parentColumns = ["id"],
      childColumns = ["jobId"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [
    Index(value = ["jobId"]),
    Index(value = ["date"]),
    Index(value = ["type"]),
  ],
)
data class ExpenseEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val jobId: String,
  val type: ExpenseType = ExpenseType.OTHER,
  val vendor: String = "",
  val description: String,
  val amountCents: Long,
  val date: LocalDate = LocalDate.now(),
  val category: String = "",
  val receiptRef: String = "",
  val createdAt: Long = System.currentTimeMillis(),
)
