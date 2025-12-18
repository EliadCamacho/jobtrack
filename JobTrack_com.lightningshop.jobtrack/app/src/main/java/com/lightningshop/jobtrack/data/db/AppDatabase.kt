package com.lightningshop.jobtrack.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lightningshop.jobtrack.data.db.dao.ExpenseDao
import com.lightningshop.jobtrack.data.db.dao.InvoiceDao
import com.lightningshop.jobtrack.data.db.dao.InvoiceLineDao
import com.lightningshop.jobtrack.data.db.dao.JobDao
import com.lightningshop.jobtrack.data.db.dao.PaymentDao
import com.lightningshop.jobtrack.data.db.dao.WorkLogDao
import com.lightningshop.jobtrack.data.db.entities.ExpenseEntity
import com.lightningshop.jobtrack.data.db.entities.InvoiceEntity
import com.lightningshop.jobtrack.data.db.entities.InvoiceLineEntity
import com.lightningshop.jobtrack.data.db.entities.JobEntity
import com.lightningshop.jobtrack.data.db.entities.PaymentEntity
import com.lightningshop.jobtrack.data.db.entities.WorkLogEntity

@Database(
  entities = [
    JobEntity::class,
    ExpenseEntity::class,
    WorkLogEntity::class,
    InvoiceEntity::class,
    InvoiceLineEntity::class,
    PaymentEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun jobDao(): JobDao
  abstract fun expenseDao(): ExpenseDao
  abstract fun workLogDao(): WorkLogDao

  abstract fun invoiceDao(): InvoiceDao
  abstract fun invoiceLineDao(): InvoiceLineDao
  abstract fun paymentDao(): PaymentDao
}
