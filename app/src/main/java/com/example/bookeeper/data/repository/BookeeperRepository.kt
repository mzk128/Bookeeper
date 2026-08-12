package com.example.bookeeper.data.repository

import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.PeriodSummary
import com.example.bookeeper.domain.model.TransactionFilter
import com.example.bookeeper.domain.model.TransactionRecord
import com.example.bookeeper.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface BookeeperRepository {
    fun observeActiveCategories(type: TransactionType): Flow<List<Category>>

    fun observeAllCategories(): Flow<List<Category>>

    fun observeActiveAccounts(): Flow<List<Account>>

    fun observeAllAccounts(): Flow<List<Account>>

    fun observeTransactions(filter: TransactionFilter): Flow<List<TransactionRecord>>

    fun observeRecentTransactions(limit: Int): Flow<List<TransactionRecord>>

    fun observePeriodSummary(
        startMillis: Long,
        endExclusiveMillis: Long,
    ): Flow<PeriodSummary>

    suspend fun getCategory(id: Long): Category?

    suspend fun getAccount(id: Long): Account?

    suspend fun getTransaction(id: Long): TransactionRecord?

    suspend fun addCategory(category: Category): Long

    suspend fun updateCategory(category: Category): Boolean

    suspend fun archiveCategory(id: Long, archived: Boolean, updatedAtMillis: Long): Boolean

    suspend fun addAccount(account: Account): Long

    suspend fun updateAccount(account: Account): Boolean

    suspend fun archiveAccount(id: Long, archived: Boolean, updatedAtMillis: Long): Boolean

    suspend fun addTransaction(transaction: TransactionRecord): Long

    suspend fun updateTransaction(transaction: TransactionRecord): Boolean

    suspend fun deleteTransaction(transaction: TransactionRecord): Boolean
}
