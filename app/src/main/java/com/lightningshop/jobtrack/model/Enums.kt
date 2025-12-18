package com.lightningshop.jobtrack.model

enum class JobStatus {
  PLANNED,
  IN_PROGRESS,
  ON_HOLD,
  DONE,
  CANCELED,
}

enum class ExpenseType {
  MATERIAL,
  LABOR,
  PERMIT,
  FUEL,
  SUBCONTRACTOR,
  OTHER,
}

enum class InvoiceStatus {
  DRAFT,
  SENT,
  PAID,
  PARTIAL,
  VOID,
}

enum class PaymentMethod {
  CASH,
  CHECK,
  ZELLE,
  CARD,
  ACH,
  OTHER,
}
