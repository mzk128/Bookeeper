package com.example.bookeeper.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bookeeper.data.local.entity.AccountEntity
import com.example.bookeeper.data.local.entity.CategoryEntity
import com.example.bookeeper.data.local.entity.TransactionEntity
import com.example.bookeeper.domain.model.AccountType
import com.example.bookeeper.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookeeperDaoTest {
    private lateinit var database: BookeeperDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BookeeperDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun transactions_canBePersistedReadUpdatedAndDeleted() = runBlocking {
        val categoryId = insertCategory("餐饮", TransactionType.EXPENSE)
        val accountId = insertAccount("现金")
        val transactionId = database.transactionDao().insert(
            transaction(categoryId, accountId, TransactionType.EXPENSE, 2_568L, 100L),
        )

        val inserted = database.transactionDao().getById(transactionId)
        assertNotNull(inserted)
        assertEquals(2_568L, inserted?.amountInCents)

        val updated = inserted!!.copy(amountInCents = 3_000L, updatedAtMillis = 200L)
        assertEquals(1, database.transactionDao().update(updated))
        assertEquals(3_000L, database.transactionDao().getById(transactionId)?.amountInCents)

        assertEquals(1, database.transactionDao().delete(updated))
        assertEquals(null, database.transactionDao().getById(transactionId))
    }

    @Test
    fun transactionForeignKeys_rejectMissingReferencesAndReferencedParentDeletion() = runBlocking {
        val categoryId = insertCategory("交通", TransactionType.EXPENSE)
        val accountId = insertAccount("银行卡", AccountType.BANK_CARD)

        expectConstraintFailure {
            database.transactionDao().insert(
                transaction(999L, accountId, TransactionType.EXPENSE, 100L, 100L),
            )
        }

        database.transactionDao().insert(
            transaction(categoryId, accountId, TransactionType.EXPENSE, 100L, 100L),
        )

        val category = database.categoryDao().getById(categoryId)!!
        expectConstraintFailure { database.categoryDao().delete(category) }
    }

    @Test
    fun filteredQuery_combinesHalfOpenTimeTypeCategoryAndAccountFilters() = runBlocking {
        val foodId = insertCategory("餐饮", TransactionType.EXPENSE)
        val salaryId = insertCategory("工资", TransactionType.INCOME)
        val cashId = insertAccount("现金")
        val bankId = insertAccount("银行卡", AccountType.BANK_CARD)

        val expectedId = database.transactionDao().insert(
            transaction(foodId, cashId, TransactionType.EXPENSE, 500L, 1_000L),
        )
        database.transactionDao().insert(
            transaction(foodId, bankId, TransactionType.EXPENSE, 600L, 1_500L),
        )
        database.transactionDao().insert(
            transaction(salaryId, cashId, TransactionType.INCOME, 10_000L, 1_500L),
        )
        database.transactionDao().insert(
            transaction(foodId, cashId, TransactionType.EXPENSE, 700L, 2_000L),
        )

        val result = database.transactionDao().observeFiltered(
            startMillis = 1_000L,
            endExclusiveMillis = 2_000L,
            type = TransactionType.EXPENSE,
            categoryId = foodId,
            accountId = cashId,
        ).first()

        assertEquals(listOf(expectedId), result.map { it.id })
    }

    @Test
    fun periodSummary_separatesIncomeAndExpenseAndCalculatesZeroForEmptyPeriod() = runBlocking {
        val expenseId = insertCategory("餐饮", TransactionType.EXPENSE)
        val incomeId = insertCategory("工资", TransactionType.INCOME)
        val accountId = insertAccount("现金")

        database.transactionDao().insert(
            transaction(incomeId, accountId, TransactionType.INCOME, 10_000L, 1_000L),
        )
        database.transactionDao().insert(
            transaction(expenseId, accountId, TransactionType.EXPENSE, 2_500L, 1_100L),
        )
        database.transactionDao().insert(
            transaction(expenseId, accountId, TransactionType.EXPENSE, 500L, 2_000L),
        )

        val summary = database.transactionDao().observePeriodSummary(1_000L, 2_000L).first()
        assertEquals(10_000L, summary.incomeInCents)
        assertEquals(2_500L, summary.expenseInCents)
        assertEquals(7_500L, summary.balanceInCents)

        val emptySummary = database.transactionDao().observePeriodSummary(3_000L, 4_000L).first()
        assertEquals(0L, emptySummary.incomeInCents)
        assertEquals(0L, emptySummary.expenseInCents)
    }

    private suspend fun insertCategory(name: String, type: TransactionType): Long =
        database.categoryDao().insert(
            CategoryEntity(
                name = name,
                transactionType = type,
                iconKey = "test_category",
                colorArgb = 0L,
                isBuiltIn = false,
                sortOrder = 0,
                createdAtMillis = 0L,
                updatedAtMillis = 0L,
            ),
        )

    private suspend fun insertAccount(
        name: String,
        type: AccountType = AccountType.CASH,
    ): Long = database.accountDao().insert(
        AccountEntity(
            name = name,
            type = type,
            initialBalanceInCents = 0L,
            iconKey = "test_account",
            colorArgb = 0L,
            isBuiltIn = false,
            sortOrder = 0,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        ),
    )

    private fun transaction(
        categoryId: Long,
        accountId: Long,
        type: TransactionType,
        amountInCents: Long,
        occurredAtMillis: Long,
    ) = TransactionEntity(
        type = type,
        amountInCents = amountInCents,
        categoryId = categoryId,
        accountId = accountId,
        occurredAtMillis = occurredAtMillis,
        createdAtMillis = occurredAtMillis,
        updatedAtMillis = occurredAtMillis,
    )

    private suspend fun expectConstraintFailure(block: suspend () -> Unit) {
        try {
            block()
            fail("Expected SQLite foreign key constraint failure")
        } catch (_: SQLiteConstraintException) {
            // Expected.
        }
    }
}
