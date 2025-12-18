package com.lightningshop.jobtrack.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lightningshop.jobtrack.data.db.entities.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
  @Query("SELECT * FROM invoices ORDER BY issueDate DESC, updatedAt DESC")
  fun observeAll(): Flow<List<InvoiceEntity>>

  @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
  fun observeById(id: String): Flow<InvoiceEntity?>

  @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
  suspend fun getById(id: String): InvoiceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(invoice: InvoiceEntity)

  @Update
  suspend fun update(invoice: InvoiceEntity)

  @Query("DELETE FROM invoices WHERE id = :id")
  suspend fun delete(id: String)
}
