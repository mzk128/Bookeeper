package com.example.bookeeper.data.repository

import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.PeriodSummary
import com.example.bookeeper.domain.model.TransactionFilter
import com.example.bookeeper.domain.model.TransactionRecord
import com.example.bookeeper.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBookeeperRepository(
    categories: List<Category> = emptyList(),
    accounts: List<Account> = emptyList(),
    transactions: List<TransactionRecord> = emptyList(),
) : BookeeperRepository {
    val categoriesFlow = MutableStateFlow(categories)
    val accountsFlow = MutableStateFlow(accounts)
    val transactionsFlow = MutableStateFlow(transactions)
    var lastAddedTransaction: TransactionRecord? = null
    var lastUpdatedTransaction: TransactionRecord? = null
    var lastDeletedTransaction: TransactionRecord? = null
    var nextTransactionId: Long = 1L
    var addTransactionFailure: Throwable? = null

    override fun observeActiveCategories(type: TransactionType): Flow<List<Category>> =
        categoriesFlow.map { categories ->
            categories.filter { it.transactionType == type && !it.isArchived }
        }

    override fun observeAllCategories(): Flow<List<Category>> = categoriesFlow

    override fun observeActiveAccounts(): Flow<List<Account>> =
        accountsFlow.map { accounts -> accounts.filter { !it.isArchived } }

    override fun observeAllAccounts(): Flow<List<Account>> = accountsFlow

    override fun observeTransactions(filter: TransactionFilter): Flow<List<TransactionRecord>> =
        transactionsFlow.map { transactions ->
            transactions.filter { transaction ->
                transaction.occurredAtMillis >= filter.startMillis &&
                    transaction.occurredAtMillis < filter.endExclusiveMillis &&
                    (filter.type == null || transaction.type == filter.type) &&
                    (filter.categoryId == null || transaction.categoryId == filter.categoryId) &&
                    (filter.accountId == null || transaction.accountId == filter.accountId)
            }
        }

    override fun observeTransaction(id: Long): Flow<TransactionRecord?> =
        transactionsFlow.map { transactions -> transactions.firstOrNull { it.id == id } }

    override fun observeRecentTransactions(limit: Int): Flow<List<TransactionRecord>> =
        transactionsFlow.map { it.take(limit) }

    override fun observePeriodSummary(
        startMillis: Long,
        endExclusiveMillis: Long,
    ): Flow<PeriodSummary> = transactionsFlow.map { transactions ->
        val inRange = transactions.filter {
            it.occurredAtMillis >= startMillis && it.occurredAtMillis < endExclusiveMillis
        }
        PeriodSummary(
            income = com.example.bookeeper.domain.model.Money(
                inRange.filter { it.type == TransactionType.INCOME }.sumOf { it.amount.cents },
            ),
            expense = com.example.bookeeper.domain.model.Money(
                inRange.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount.cents },
            ),
        )
    }

    override suspend fun getCategory(id: Long): Category? =
        categoriesFlow.value.firstOrNull { it.id == id }

    override suspend fun getAccount(id: Long): Account? =
        accountsFlow.value.firstOrNull { it.id == id }

    override suspend fun getTransaction(id: Long): TransactionRecord? =
        transactionsFlow.value.firstOrNull { it.id == id }

    override suspend fun addCategory(category: Category): Long = throw UnsupportedOperationException()

    override suspend fun updateCategory(category: Category): Boolean =
        throw UnsupportedOperationException()

    override suspend fun archiveCategory(
        id: Long,
        archived: Boolean,
        updatedAtMillis: Long,
    ): Boolean = throw UnsupportedOperationException()

    override suspend fun addAccount(account: Account): Long = throw UnsupportedOperationException()

    override suspend fun updateAccount(account: Account): Boolean =
        throw UnsupportedOperationException()

    override suspend fun archiveAccount(
        id: Long,
        archived: Boolean,
        updatedAtMillis: Long,
    ): Boolean = throw UnsupportedOperationException()

    override suspend fun addTransaction(transaction: TransactionRecord): Long {
        addTransactionFailure?.let { throw it }
        lastAddedTransaction = transaction
        val saved = transaction.copy(id = nextTransactionId)
        transactionsFlow.value = listOf(saved) + transactionsFlow.value
        return nextTransactionId
    }

    override suspend fun updateTransaction(transaction: TransactionRecord): Boolean {
        val existing = transactionsFlow.value.indexOfFirst { it.id == transaction.id }
        if (existing == -1) return false
        lastUpdatedTransaction = transaction
        transactionsFlow.value = transactionsFlow.value.toMutableList().apply {
            set(existing, transaction)
        }
        return true
    }

    override suspend fun deleteTransaction(transaction: TransactionRecord): Boolean {
        if (transactionsFlow.value.none { it.id == transaction.id }) return false
        lastDeletedTransaction = transaction
        transactionsFlow.value = transactionsFlow.value.filterNot { it.id == transaction.id }
        return true
    }
}
