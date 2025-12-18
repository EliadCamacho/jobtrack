package com.lightningshop.jobtrack.data.repo

import com.lightningshop.jobtrack.data.db.dao.ExpenseDao
import com.lightningshop.jobtrack.data.db.dao.JobDao
import com.lightningshop.jobtrack.data.db.dao.WorkLogDao
import com.lightningshop.jobtrack.data.db.entities.ExpenseEntity
import com.lightningshop.jobtrack.data.db.entities.JobEntity
import com.lightningshop.jobtrack.data.db.entities.WorkLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class JobSummary(
  val job: JobEntity,
  val totalExpensesCents: Long,
  val totalLaborCents: Long,
)

class JobsRepository(
  private val jobDao: JobDao,
  private val expenseDao: ExpenseDao,
  private val workLogDao: WorkLogDao,
) {

  fun observeJobs(): Flow<List<JobEntity>> = jobDao.observeAll()

  fun observeJob(id: String): Flow<JobEntity?> = jobDao.observeById(id)

  fun observeJobSummary(id: String): Flow<JobSummary?> =
    combine(
      jobDao.observeById(id),
      expenseDao.observeTotalCentsForJob(id),
      workLogDao.observeForJob(id),
    ) { job, expensesCents, workLogs ->
      job ?: return@combine null
      val laborCents = workLogs.sumOf { ((it.hours * (it.rateCentsPerHour / 100.0)) * 100).toLong() }
      JobSummary(job, expensesCents, laborCents)
    }

  fun observeExpenses(jobId: String): Flow<List<ExpenseEntity>> = expenseDao.observeForJob(jobId)

  fun observeWorkLogs(jobId: String): Flow<List<WorkLogEntity>> = workLogDao.observeForJob(jobId)

  suspend fun upsertJob(job: JobEntity) = jobDao.upsert(job)

  suspend fun deleteJob(id: String) = jobDao.delete(id)

  suspend fun addExpense(expense: ExpenseEntity) = expenseDao.insert(expense)

  suspend fun deleteExpense(id: String) = expenseDao.delete(id)

  suspend fun addWorkLog(log: WorkLogEntity) = workLogDao.insert(log)

  suspend fun deleteWorkLog(id: String) = workLogDao.delete(id)

  suspend fun getJob(id: String): JobEntity? = jobDao.getById(id)
}
