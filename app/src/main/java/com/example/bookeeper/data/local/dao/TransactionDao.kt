package com.example.bookeeper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.bookeeper.data.local.converter.BookeeperTypeConverters
import com.example.bookeeper.data.local.entity.TransactionEntity
import com.example.bookeeper.data.local.model.PeriodSummary
import com.example.bookeeper.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(BookeeperTypeConverters::class)
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity): Int

    @Delete
    suspend fun delete(transaction: TransactionEntity): Int

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT * FROM transactions
        ORDER BY occurredAtMillis DESC, id DESC
        """,
    )
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE occurredAtMillis >= :startMillis
          AND occurredAtMillis < :endExclusiveMillis
          AND (:type IS NULL OR type = :type)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:accountId IS NULL OR accountId = :accountId)
        ORDER BY occurredAtMillis DESC, id DESC
        """,
    )
    fun observeFiltered(
        startMillis: Long,
        endExclusiveMillis: Long,
        type: TransactionType? = null,
        categoryId: Long? = null,
        accountId: Long? = null,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        ORDER BY occurredAtMillis DESC, id DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'income' THEN amountInCents ELSE 0 END), 0) AS incomeInCents,
            COALESCE(SUM(CASE WHEN type = 'expense' THEN amountInCents ELSE 0 END), 0) AS expenseInCents
        FROM transactions
        WHERE occurredAtMillis >= :startMillis
          AND occurredAtMillis < :endExclusiveMillis
        """,
    )
    fun observePeriodSummary(
        startMillis: Long,
        endExclusiveMillis: Long,
    ): Flow<PeriodSummary>
}
