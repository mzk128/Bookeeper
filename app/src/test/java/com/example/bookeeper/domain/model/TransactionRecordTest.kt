package com.example.bookeeper.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionRecordTest {
    @Test
    fun acceptsPositiveAmountAndStableReferences() {
        val record = TransactionRecord(
            id = 1L,
            type = TransactionType.EXPENSE,
            amount = Money(2_568L),
            categoryId = 2L,
            accountId = 3L,
            note = "午餐",
            occurredAtMillis = 1_700_000_000_000L,
            createdAtMillis = 1_700_000_000_000L,
            updatedAtMillis = 1_700_000_000_000L,
        )

        assertEquals(2_568L, record.amount.cents)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroAmount() {
        TransactionRecord(
            id = 0L,
            type = TransactionType.INCOME,
            amount = Money.Zero,
            categoryId = 1L,
            accountId = 1L,
            note = "",
            occurredAtMillis = 0L,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        )
    }
}
