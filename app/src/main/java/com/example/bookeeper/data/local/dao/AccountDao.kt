package com.example.bookeeper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bookeeper.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity): Int

    @Delete
    suspend fun delete(account: AccountEntity): Int

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): AccountEntity?

    @Query(
        """
        SELECT * FROM accounts
        WHERE isArchived = 0
        ORDER BY sortOrder ASC, name COLLATE NOCASE ASC, id ASC
        """,
    )
    fun observeActive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query(
        """
        UPDATE accounts
        SET isArchived = :isArchived, updatedAtMillis = :updatedAtMillis
        WHERE id = :id
        """,
    )
    suspend fun setArchived(id: Long, isArchived: Boolean, updatedAtMillis: Long): Int
}
