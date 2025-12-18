package com.lightningshop.jobtrack.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lightningshop.jobtrack.model.JobStatus
import java.time.LocalDate
import java.util.UUID

@Entity(
  tableName = "jobs",
  indices = [
    Index(value = ["createdAt"]),
    Index(value = ["status"]),
  ],
)
data class JobEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val title: String,
  val contractor: String = "",
  val customer: String = "",
  val address: String = "",
  val status: JobStatus = JobStatus.PLANNED,
  val startDate: LocalDate? = null,
  val dueDate: LocalDate? = null,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)
