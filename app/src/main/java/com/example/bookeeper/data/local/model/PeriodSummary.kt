package com.example.bookeeper.data.local.model

data class PeriodSummary(
    val incomeInCents: Long,
    val expenseInCents: Long,
) {
    val balanceInCents: Long
        get() = incomeInCents - expenseInCents
}
