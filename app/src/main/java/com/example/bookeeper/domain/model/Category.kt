package com.example.bookeeper.domain.model

data class Category(
    val id: Long,
    val name: String,
    val transactionType: TransactionType,
    val iconKey: String,
    val colorArgb: Long,
    val isBuiltIn: Boolean,
    val isArchived: Boolean,
    val sortOrder: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    init {
        require(name.isNotBlank()) { "Category name must not be blank" }
        require(iconKey.isNotBlank()) { "Category icon key must not be blank" }
        require(createdAtMillis >= 0L) { "Creation time must not be negative" }
        require(updatedAtMillis >= createdAtMillis) {
            "Update time must not be earlier than creation time"
        }
    }
}
