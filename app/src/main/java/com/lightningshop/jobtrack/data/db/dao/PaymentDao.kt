package com.lightningshop.jobtrack.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lightningshop.jobtrack.data.db.entities.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
  @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY date DESC, createdAt DESC")
  fun observeForInvoice(invoiceId: String): Flow<List<PaymentEntity>>

  @Query("SELECT COALESCE(SUM(amountCents), 0) FROM payments WHERE invoiceId = :invoiceId")
  fun observePaidCents(invoiceId: String): Flow<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(payment: PaymentEntity)

  @Query("DELETE FROM payments WHERE id = :id")
  suspend fun delete(id: String)
}
