package com.example.bookeeper.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bookeeper.domain.model.TransactionType
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultDataInitializationTest {
    private lateinit var context: Context
    private lateinit var databaseName: String
    private lateinit var database: BookeeperDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        databaseName = "default-data-${UUID.randomUUID()}.db"
        database = BookeeperDatabase.build(context, databaseName)
        database.openHelper.writableDatabase
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun firstCreation_insertsBuiltInCategoriesAndCashAccount() = runBlocking {
        val expenseCategories = database.categoryDao()
            .observeActive(TransactionType.EXPENSE)
            .first()
        val incomeCategories = database.categoryDao()
            .observeActive(TransactionType.INCOME)
            .first()
        val accounts = database.accountDao().observeActive().first()

        assertEquals(8, expenseCategories.size)
        assertEquals(5, incomeCategories.size)
        assertTrue((expenseCategories + incomeCategories).all { it.isBuiltIn })
        assertEquals("餐饮", expenseCategories.first().name)
        assertEquals("工资", incomeCategories.first().name)
        assertEquals(1, accounts.size)
        assertEquals("现金", accounts.single().name)
        assertTrue(accounts.single().isBuiltIn)
    }
}
