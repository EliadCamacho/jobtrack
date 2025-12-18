package com.lightningshop.jobtrack.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lightningshop.jobtrack.data.db.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses WHERE jobId = :jobId ORDER BY date DESC, createdAt DESC")
  fun observeForJob(jobId: String): Flow<List<ExpenseEntity>>

  @Query("SELECT COALESCE(SUM(amountCents), 0) FROM expenses WHERE jobId = :jobId")
  fun observeTotalCentsForJob(jobId: String): Flow<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(expense: ExpenseEntity)

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun delete(id: String)
}
