package com.example.bookeeper.ui.transaction

import com.example.bookeeper.MainDispatcherRule
import com.example.bookeeper.data.repository.FakeBookeeperRepository
import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.AccountType
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionRecord
import com.example.bookeeper.domain.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun delete_removesTransactionAndPublishesDeletedState() =
        runTest(mainDispatcherRule.testDispatcher) {
            val transaction = TransactionRecord(9L, TransactionType.EXPENSE, Money(500L), 1L, 1L, "", 1_000L, 1_000L, 1_000L)
            val repository = FakeBookeeperRepository(
                categories = listOf(Category(1L, "餐饮", TransactionType.EXPENSE, "x", 0L, true, false, 0, 0L, 0L)),
                accounts = listOf(Account(1L, "现金", AccountType.CASH, Money.Zero, "x", 0L, true, false, 0, 0L, 0L)),
                transactions = listOf(transaction),
            )
            val viewModel = TransactionDetailViewModel(repository, transaction.id)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            assertEquals("餐饮", viewModel.uiState.value.categoryName)
            viewModel.delete()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.deleted)
            assertEquals(transaction.id, repository.lastDeletedTransaction?.id)
            assertNull(repository.transactionsFlow.value.firstOrNull())
        }
}
