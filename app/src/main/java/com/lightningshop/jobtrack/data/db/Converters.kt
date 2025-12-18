package com.lightningshop.jobtrack.data.db

import androidx.room.TypeConverter
import com.lightningshop.jobtrack.model.ExpenseType
import com.lightningshop.jobtrack.model.InvoiceStatus
import com.lightningshop.jobtrack.model.JobStatus
import com.lightningshop.jobtrack.model.PaymentMethod
import java.time.LocalDate

class Converters {

  // LocalDate
  @TypeConverter
  fun localDateToString(d: LocalDate?): String? = d?.toString()

  @TypeConverter
  fun stringToLocalDate(s: String?): LocalDate? = s?.let { LocalDate.parse(it) }

  // Enums
  @TypeConverter
  fun jobStatusToString(v: JobStatus?): String? = v?.name

  @TypeConverter
  fun stringToJobStatus(s: String?): JobStatus? = s?.let { JobStatus.valueOf(it) }

  @TypeConverter
  fun expenseTypeToString(v: ExpenseType?): String? = v?.name

  @TypeConverter
  fun stringToExpenseType(s: String?): ExpenseType? = s?.let { ExpenseType.valueOf(it) }

  @TypeConverter
  fun invoiceStatusToString(v: InvoiceStatus?): String? = v?.name

  @TypeConverter
  fun stringToInvoiceStatus(s: String?): InvoiceStatus? = s?.let { InvoiceStatus.valueOf(it) }

  @TypeConverter
  fun paymentMethodToString(v: PaymentMethod?): String? = v?.name

  @TypeConverter
  fun stringToPaymentMethod(s: String?): PaymentMethod? = s?.let { PaymentMethod.valueOf(it) }
}
