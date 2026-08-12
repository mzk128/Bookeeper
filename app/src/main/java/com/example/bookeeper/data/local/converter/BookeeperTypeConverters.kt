package com.example.bookeeper.data.local.converter

import androidx.room.TypeConverter
import com.example.bookeeper.domain.model.AccountType
import com.example.bookeeper.domain.model.TransactionType

class BookeeperTypeConverters {
    @TypeConverter
    fun transactionTypeToStorageValue(value: TransactionType): String = value.storageValue

    @TypeConverter
    fun storageValueToTransactionType(value: String): TransactionType =
        TransactionType.fromStorageValue(value)

    @TypeConverter
    fun accountTypeToStorageValue(value: AccountType): String = value.storageValue

    @TypeConverter
    fun storageValueToAccountType(value: String): AccountType =
        AccountType.fromStorageValue(value)
}
