package com.lightningshop.jobtrack.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lightningshop.jobtrack.data.db.entities.WorkLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkLogDao {
  @Query("SELECT * FROM work_logs WHERE jobId = :jobId ORDER BY date DESC, createdAt DESC")
  fun observeForJob(jobId: String): Flow<List<WorkLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(log: WorkLogEntity)

  @Query("DELETE FROM work_logs WHERE id = :id")
  suspend fun delete(id: String)
}
