package com.example.bookeeper.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bookeeper.domain.model.AccountType
import com.example.bookeeper.domain.model.TransactionType

/** Inserts the deterministic version 1 starter data when the database is first created. */
internal object DefaultDataCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        DefaultDataInitializer.initialize(db)
    }
}

internal object DefaultDataInitializer {
    private data class CategorySeed(
        val id: Long,
        val name: String,
        val type: TransactionType,
        val iconKey: String,
        val colorArgb: Long,
        val sortOrder: Int,
    )

    private val categories = listOf(
        CategorySeed(1L, "餐饮", TransactionType.EXPENSE, "restaurant", 0xFFFF7043L, 10),
        CategorySeed(2L, "交通", TransactionType.EXPENSE, "directions_car", 0xFF42A5F5L, 20),
        CategorySeed(3L, "购物", TransactionType.EXPENSE, "shopping_bag", 0xFFAB47BCL, 30),
        CategorySeed(4L, "居住", TransactionType.EXPENSE, "home", 0xFF8D6E63L, 40),
        CategorySeed(5L, "娱乐", TransactionType.EXPENSE, "movie", 0xFF7E57C2L, 50),
        CategorySeed(6L, "医疗", TransactionType.EXPENSE, "medical_services", 0xFFEF5350L, 60),
        CategorySeed(7L, "教育", TransactionType.EXPENSE, "school", 0xFF26A69AL, 70),
        CategorySeed(8L, "其他支出", TransactionType.EXPENSE, "more_horiz", 0xFF78909CL, 80),
        CategorySeed(101L, "工资", TransactionType.INCOME, "payments", 0xFF66BB6AL, 10),
        CategorySeed(102L, "奖金", TransactionType.INCOME, "card_giftcard", 0xFF9CCC65L, 20),
        CategorySeed(103L, "理财", TransactionType.INCOME, "trending_up", 0xFF26A69AL, 30),
        CategorySeed(104L, "兼职", TransactionType.INCOME, "work", 0xFF5C6BC0L, 40),
        CategorySeed(105L, "其他收入", TransactionType.INCOME, "more_horiz", 0xFF78909CL, 50),
    )

    fun initialize(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            categories.forEach { category -> insertCategory(db, category) }
            insertDefaultAccount(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun insertCategory(db: SupportSQLiteDatabase, category: CategorySeed) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO categories (
                id, name, transactionType, iconKey, colorArgb,
                isBuiltIn, isArchived, sortOrder, createdAtMillis, updatedAtMillis
            ) VALUES (?, ?, ?, ?, ?, 1, 0, ?, 0, 0)
            """.trimIndent(),
            arrayOf<Any?>(
                category.id,
                category.name,
                category.type.storageValue,
                category.iconKey,
                category.colorArgb,
                category.sortOrder,
            ),
        )
    }

    private fun insertDefaultAccount(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT OR IGNORE INTO accounts (
                id, name, type, initialBalanceInCents, iconKey, colorArgb,
                isBuiltIn, isArchived, sortOrder, createdAtMillis, updatedAtMillis
            ) VALUES (?, ?, ?, 0, ?, ?, 1, 0, 10, 0, 0)
            """.trimIndent(),
            arrayOf<Any?>(
                1L,
                "现金",
                AccountType.CASH.storageValue,
                "wallet",
                0xFF66BB6AL,
            ),
        )
    }
}
