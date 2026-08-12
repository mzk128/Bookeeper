package com.example.bookeeper.navigation

object AppDestination {
    const val ADD_TRANSACTION = "add_transaction"
    const val TRANSACTION_ID_ARGUMENT = "transactionId"
    const val TRANSACTION_DETAIL_PATTERN = "transaction/{$TRANSACTION_ID_ARGUMENT}"
    const val EDIT_TRANSACTION_PATTERN = "transaction/{$TRANSACTION_ID_ARGUMENT}/edit"

    fun transactionDetail(transactionId: Long) = "transaction/$transactionId"

    fun editTransaction(transactionId: Long) = "transaction/$transactionId/edit"
}
