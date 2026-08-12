package com.example.bookeeper.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookeeper.data.local.converter.BookeeperTypeConverters
import com.example.bookeeper.data.local.dao.AccountDao
import com.example.bookeeper.data.local.dao.CategoryDao
import com.example.bookeeper.data.local.dao.TransactionDao
import com.example.bookeeper.data.local.entity.AccountEntity
import com.example.bookeeper.data.local.entity.CategoryEntity
import com.example.bookeeper.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(BookeeperTypeConverters::class)
abstract class BookeeperDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun accountDao(): AccountDao

    companion object {
        const val DATABASE_NAME = "bookeeper.db"

        @Volatile
        private var instance: BookeeperDatabase? = null

        fun getInstance(context: Context): BookeeperDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        internal fun build(
            context: Context,
            databaseName: String = DATABASE_NAME,
        ): BookeeperDatabase = Room.databaseBuilder(
            context.applicationContext,
            BookeeperDatabase::class.java,
            databaseName,
        )
            .addCallback(DefaultDataCallback)
            .build()
    }
}
