package com.example.bookeeper.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.bookeeper.data.local.converter.BookeeperTypeConverters
import com.example.bookeeper.domain.model.AccountType

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["isArchived", "sortOrder"]),
    ],
)
@TypeConverters(BookeeperTypeConverters::class)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: AccountType,
    val initialBalanceInCents: Long,
    val iconKey: String,
    val colorArgb: Long,
    val isBuiltIn: Boolean,
    val isArchived: Boolean = false,
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
