package com.lightningshop.jobtrack.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lightningshop.jobtrack.data.db.entities.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
  @Query("SELECT * FROM jobs ORDER BY updatedAt DESC")
  fun observeAll(): Flow<List<JobEntity>>

  @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
  fun observeById(id: String): Flow<JobEntity?>

  @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
  suspend fun getById(id: String): JobEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(job: JobEntity)

  @Update
  suspend fun update(job: JobEntity)

  @Query("DELETE FROM jobs WHERE id = :id")
  suspend fun delete(id: String)
}
