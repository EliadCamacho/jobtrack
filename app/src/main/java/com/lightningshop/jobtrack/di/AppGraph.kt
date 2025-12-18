package com.lightningshop.jobtrack.di

import android.content.Context
import androidx.room.Room
import com.lightningshop.jobtrack.data.db.AppDatabase
import com.lightningshop.jobtrack.data.repo.InvoicesRepository
import com.lightningshop.jobtrack.data.repo.JobsRepository
import com.lightningshop.jobtrack.export.InvoiceExporter
import com.lightningshop.jobtrack.settings.AppPreferences

object AppGraph {
  private lateinit var appContext: Context

  lateinit var db: AppDatabase
    private set

  lateinit var prefs: AppPreferences
    private set

  lateinit var jobsRepo: JobsRepository
    private set

  lateinit var invoicesRepo: InvoicesRepository
    private set

  lateinit var export: InvoiceExporter
    private set

  fun init(context: Context) {
    if (this::db.isInitialized) return

    appContext = context.applicationContext

    db = Room.databaseBuilder(
      appContext,
      AppDatabase::class.java,
      "jobtrack.db",
    )
      .fallbackToDestructiveMigration()
      .build()

    prefs = AppPreferences(appContext)

    jobsRepo = JobsRepository(db.jobDao(), db.expenseDao(), db.workLogDao())
    invoicesRepo = InvoicesRepository(db.invoiceDao(), db.invoiceLineDao(), db.paymentDao())
    export = InvoiceExporter(appContext, prefs, invoicesRepo, jobsRepo)
  }
}
