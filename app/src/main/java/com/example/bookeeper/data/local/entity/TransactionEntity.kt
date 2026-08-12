package com.example.bookeeper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.bookeeper.data.local.converter.BookeeperTypeConverters
import com.example.bookeeper.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["accountId"]),
        Index(value = ["occurredAtMillis"]),
        Index(value = ["type", "occurredAtMillis"]),
    ],
)
@TypeConverters(BookeeperTypeConverters::class)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: TransactionType,
    val amountInCents: Long,
    val categoryId: Long,
    val accountId: Long,
    val note: String = "",
    val occurredAtMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    init {
        require(amountInCents > 0L) { "Transaction amount must be greater than zero" }
        require(categoryId > 0L) { "Transaction category id must be positive" }
        require(accountId > 0L) { "Transaction account id must be positive" }
        require(occurredAtMillis >= 0L) { "Occurrence time must not be negative" }
        require(createdAtMillis >= 0L) { "Creation time must not be negative" }
        require(updatedAtMillis >= createdAtMillis) {
            "Update time must not be earlier than creation time"
        }
    }
}
