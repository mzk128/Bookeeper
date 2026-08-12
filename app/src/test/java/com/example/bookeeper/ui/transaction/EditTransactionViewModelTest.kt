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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditTransactionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun existingTransaction_isLoadedAndUpdatedWithoutChangingCreationTime() =
        runTest(mainDispatcherRule.testDispatcher) {
            val original = transaction()
            val repository = FakeBookeeperRepository(
                categories = listOf(category()),
                accounts = listOf(account()),
                transactions = listOf(original),
            )
            val viewModel = AddTransactionViewModel(repository, transactionId = original.id) { NOW }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            assertEquals("12.8", viewModel.uiState.value.form.amountInput)
            assertEquals(listOf("餐饮"), viewModel.uiState.value.categories.map { it.name })
            assertEquals(listOf("现金"), viewModel.uiState.value.accounts.map { it.name })
            viewModel.updateAmount("25.68")
            viewModel.updateNote("修改后")
            viewModel.save()
            advanceUntilIdle()

            val updated = repository.lastUpdatedTransaction
            assertNull(repository.lastAddedTransaction)
            assertEquals(original.id, updated?.id)
            assertEquals(Money(2_568L), updated?.amount)
            assertEquals(original.createdAtMillis, updated?.createdAtMillis)
            assertEquals(NOW, updated?.updatedAtMillis)
        }

    private fun category() = Category(1L, "餐饮", TransactionType.EXPENSE, "test", 0L, true, true, 0, 0L, 0L)
    private fun account() = Account(1L, "现金", AccountType.CASH, Money.Zero, "test", 0L, true, true, 0, 0L, 0L)
    private fun transaction() = TransactionRecord(9L, TransactionType.EXPENSE, Money(1_280L), 1L, 1L, "午餐", 1_000L, 500L, 500L)

    private companion object { const val NOW = 2_000L }
}
