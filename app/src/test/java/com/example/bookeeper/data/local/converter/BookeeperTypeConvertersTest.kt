package com.example.bookeeper.data.local.converter

import com.example.bookeeper.domain.model.AccountType
import com.example.bookeeper.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class BookeeperTypeConvertersTest {
    private val converters = BookeeperTypeConverters()

    @Test
    fun transactionTypeRoundTripsThroughStableStorageValue() {
        TransactionType.entries.forEach { type ->
            val stored = converters.transactionTypeToStorageValue(type)
            assertEquals(type, converters.storageValueToTransactionType(stored))
        }
    }

    @Test
    fun accountTypeRoundTripsThroughStableStorageValue() {
        AccountType.entries.forEach { type ->
            val stored = converters.accountTypeToStorageValue(type)
            assertEquals(type, converters.storageValueToAccountType(stored))
        }
    }
}
