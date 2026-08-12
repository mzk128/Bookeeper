package com.example.bookeeper.ui.ledger

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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun repositoryFlow_isMappedToDisplayNamesAndUpdatesReactively() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeBookeeperRepository(
                categories = listOf(category()),
                accounts = listOf(account()),
            )
            val viewModel = LedgerViewModel(repository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            assertEquals(emptyList<LedgerItemUiState>(), viewModel.uiState.value.items)

            repository.transactionsFlow.value = listOf(transaction())
            advanceUntilIdle()

            val item = viewModel.uiState.value.items.single()
            assertEquals("餐饮", item.categoryName)
            assertEquals("现金", item.accountName)
            assertEquals(Money(1_280L), item.amount)
        }

    private fun category() = Category(
        id = 1L,
        name = "餐饮",
        transactionType = TransactionType.EXPENSE,
        iconKey = "test",
        colorArgb = 0L,
        isBuiltIn = true,
        isArchived = false,
        sortOrder = 0,
        createdAtMillis = 0L,
        updatedAtMillis = 0L,
    )

    private fun account() = Account(
        id = 1L,
        name = "现金",
        type = AccountType.CASH,
        initialBalance = Money.Zero,
        iconKey = "test",
        colorArgb = 0L,
        isBuiltIn = true,
        isArchived = false,
        sortOrder = 0,
        createdAtMillis = 0L,
        updatedAtMillis = 0L,
    )

    private fun transaction() = TransactionRecord(
        id = 1L,
        type = TransactionType.EXPENSE,
        amount = Money(1_280L),
        categoryId = 1L,
        accountId = 1L,
        note = "午餐",
        occurredAtMillis = 1_000L,
        createdAtMillis = 1_000L,
        updatedAtMillis = 1_000L,
    )
}
