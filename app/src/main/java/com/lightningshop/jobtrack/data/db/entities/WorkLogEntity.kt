package com.lightningshop.jobtrack.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(
  tableName = "work_logs",
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
  ],
)
data class WorkLogEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val jobId: String,
  val workerName: String,
  val hours: Double,
  val rateCentsPerHour: Long = 0,
  val date: LocalDate = LocalDate.now(),
  val note: String = "",
  val createdAt: Long = System.currentTimeMillis(),
)
