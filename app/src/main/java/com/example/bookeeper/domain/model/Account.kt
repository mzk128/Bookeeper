package com.example.bookeeper.domain.model

data class Account(
    val id: Long,
    val name: String,
    val type: AccountType,
    val initialBalance: Money,
    val iconKey: String,
    val colorArgb: Long,
    val isBuiltIn: Boolean,
    val isArchived: Boolean,
    val sortOrder: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank" }
        require(iconKey.isNotBlank()) { "Account icon key must not be blank" }
        require(createdAtMillis >= 0L) { "Creation time must not be negative" }
        require(updatedAtMillis >= createdAtMillis) {
            "Update time must not be earlier than creation time"
        }
    }
}
