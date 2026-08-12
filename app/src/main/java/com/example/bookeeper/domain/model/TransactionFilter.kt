package com.example.bookeeper.domain.model

data class TransactionFilter(
    val startMillis: Long,
    val endExclusiveMillis: Long,
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
) {
    init {
        require(startMillis >= 0L) { "Start time must not be negative" }
        require(endExclusiveMillis > startMillis) { "End time must be later than start time" }
        require(categoryId == null || categoryId > 0L) { "Category id must be positive" }
        require(accountId == null || accountId > 0L) { "Account id must be positive" }
    }
}
