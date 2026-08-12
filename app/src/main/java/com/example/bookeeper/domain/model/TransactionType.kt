package com.example.bookeeper.domain.model

enum class TransactionType(val storageValue: String) {
    EXPENSE("expense"),
    INCOME("income"),
    ;

    companion object {
        fun fromStorageValue(value: String): TransactionType = entries.firstOrNull {
            it.storageValue == value
        } ?: throw IllegalArgumentException("Unknown transaction type: $value")
    }
}
