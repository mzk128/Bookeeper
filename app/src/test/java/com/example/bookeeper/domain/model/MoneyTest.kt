package com.example.bookeeper.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {
    @Test
    fun arithmeticUsesExactCentValues() {
        val income = Money(10_000L)
        val expense = Money(2_568L)

        assertEquals(Money(12_568L), income + expense)
        assertEquals(Money(7_432L), income - expense)
    }

    @Test(expected = ArithmeticException::class)
    fun additionFailsInsteadOfOverflowing() {
        Money(Long.MAX_VALUE) + Money(1L)
    }
}
