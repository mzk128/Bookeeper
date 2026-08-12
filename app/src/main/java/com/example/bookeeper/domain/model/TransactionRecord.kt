package com.example.bookeeper.domain.model

data class TransactionRecord(
    val id: Long,
    val type: TransactionType,
    val amount: Money,
    val categoryId: Long,
    val accountId: Long,
    val note: String,
    val occurredAtMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    init {
        require(amount.cents > 0L) { "Transaction amount must be greater than zero" }
        require(categoryId > 0L) { "Transaction category id must be positive" }
        require(accountId > 0L) { "Transaction account id must be positive" }
        require(occurredAtMillis >= 0L) { "Occurrence time must not be negative" }
        require(createdAtMillis >= 0L) { "Creation time must not be negative" }
        require(updatedAtMillis >= createdAtMillis) {
            "Update time must not be earlier than creation time"
        }
    }
}
