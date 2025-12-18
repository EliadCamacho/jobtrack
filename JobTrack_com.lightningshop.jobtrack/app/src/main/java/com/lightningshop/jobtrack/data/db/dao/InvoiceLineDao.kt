package com.lightningshop.jobtrack.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lightningshop.jobtrack.data.db.entities.InvoiceLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceLineDao {
  @Query("SELECT * FROM invoice_lines WHERE invoiceId = :invoiceId ORDER BY sortOrder ASC, createdAt ASC")
  fun observeForInvoice(invoiceId: String): Flow<List<InvoiceLineEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(line: InvoiceLineEntity)

  @Query("DELETE FROM invoice_lines WHERE id = :id")
  suspend fun delete(id: String)

  @Query("DELETE FROM invoice_lines WHERE invoiceId = :invoiceId")
  suspend fun deleteAllForInvoice(invoiceId: String)
}
