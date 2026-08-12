package com.example.bookeeper.data.repository

import com.example.bookeeper.data.local.BookeeperDatabase
import com.example.bookeeper.data.local.dao.AccountDao
import com.example.bookeeper.data.local.dao.CategoryDao
import com.example.bookeeper.data.local.dao.TransactionDao
import com.example.bookeeper.data.mapper.toDomain
import com.example.bookeeper.data.mapper.toEntity
import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.PeriodSummary
import com.example.bookeeper.domain.model.TransactionFilter
import com.example.bookeeper.domain.model.TransactionRecord
import com.example.bookeeper.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineBookeeperRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
) : BookeeperRepository {
    constructor(database: BookeeperDatabase) : this(
        transactionDao = database.transactionDao(),
        categoryDao = database.categoryDao(),
        accountDao = database.accountDao(),
    )

    override fun observeActiveCategories(type: TransactionType): Flow<List<Category>> =
        categoryDao.observeActive(type).map { entities -> entities.map { it.toDomain() } }

    override fun observeAllCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeActiveAccounts(): Flow<List<Account>> =
        accountDao.observeActive().map { entities -> entities.map { it.toDomain() } }

    override fun observeAllAccounts(): Flow<List<Account>> =
        accountDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeTransactions(
        filter: TransactionFilter,
    ): Flow<List<TransactionRecord>> = transactionDao.observeFiltered(
        startMillis = filter.startMillis,
        endExclusiveMillis = filter.endExclusiveMillis,
        type = filter.type,
        categoryId = filter.categoryId,
        accountId = filter.accountId,
    ).map { entities -> entities.map { it.toDomain() } }

    override fun observeTransaction(id: Long): Flow<TransactionRecord?> {
        require(id > 0L) { "Transaction id must be positive" }
        return transactionDao.observeById(id).map { it?.toDomain() }
    }

    override fun observeRecentTransactions(limit: Int): Flow<List<TransactionRecord>> {
        require(limit > 0) { "Recent transaction limit must be positive" }
        return transactionDao.observeRecent(limit).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observePeriodSummary(
        startMillis: Long,
        endExclusiveMillis: Long,
    ): Flow<PeriodSummary> {
        require(startMillis >= 0L) { "Start time must not be negative" }
        require(endExclusiveMillis > startMillis) { "End time must be later than start time" }
        return transactionDao.observePeriodSummary(startMillis, endExclusiveMillis).map { summary ->
            PeriodSummary(
                income = Money(summary.incomeInCents),
                expense = Money(summary.expenseInCents),
            )
        }
    }

    override suspend fun getCategory(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    override suspend fun getAccount(id: Long): Account? =
        accountDao.getById(id)?.toDomain()

    override suspend fun getTransaction(id: Long): TransactionRecord? =
        transactionDao.getById(id)?.toDomain()

    override suspend fun addCategory(category: Category): Long =
        categoryDao.insert(category.toEntity())

    override suspend fun updateCategory(category: Category): Boolean =
        categoryDao.update(category.toEntity()) == 1

    override suspend fun archiveCategory(
        id: Long,
        archived: Boolean,
        updatedAtMillis: Long,
    ): Boolean = categoryDao.setArchived(id, archived, updatedAtMillis) == 1

    override suspend fun addAccount(account: Account): Long =
        accountDao.insert(account.toEntity())

    override suspend fun updateAccount(account: Account): Boolean =
        accountDao.update(account.toEntity()) == 1

    override suspend fun archiveAccount(
        id: Long,
        archived: Boolean,
        updatedAtMillis: Long,
    ): Boolean = accountDao.setArchived(id, archived, updatedAtMillis) == 1

    override suspend fun addTransaction(transaction: TransactionRecord): Long =
        transactionDao.insert(transaction.toEntity())

    override suspend fun updateTransaction(transaction: TransactionRecord): Boolean =
        transactionDao.update(transaction.toEntity()) == 1

    override suspend fun deleteTransaction(transaction: TransactionRecord): Boolean =
        transactionDao.delete(transaction.toEntity()) == 1
}
