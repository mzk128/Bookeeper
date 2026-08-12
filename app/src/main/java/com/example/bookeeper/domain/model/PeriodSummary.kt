package com.example.bookeeper.domain.model

data class PeriodSummary(
    val income: Money,
    val expense: Money,
) {
    val balance: Money
        get() = income - expense
}
