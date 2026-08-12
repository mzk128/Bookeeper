package com.example.bookeeper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.bookeeper.data.local.converter.BookeeperTypeConverters
import com.example.bookeeper.data.local.entity.CategoryEntity
import com.example.bookeeper.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(BookeeperTypeConverters::class)
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity): Int

    @Delete
    suspend fun delete(category: CategoryEntity): Int

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoryEntity?

    @Query(
        """
        SELECT * FROM categories
        WHERE transactionType = :transactionType AND isArchived = 0
        ORDER BY sortOrder ASC, name COLLATE NOCASE ASC, id ASC
        """,
    )
    fun observeActive(transactionType: TransactionType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY transactionType ASC, sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query(
        """
        UPDATE categories
        SET isArchived = :isArchived, updatedAtMillis = :updatedAtMillis
        WHERE id = :id
        """,
    )
    suspend fun setArchived(id: Long, isArchived: Boolean, updatedAtMillis: Long): Int
}
