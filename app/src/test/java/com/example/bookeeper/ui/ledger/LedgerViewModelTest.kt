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
import java.util.Calendar

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

    @Test
    fun combinedFilters_includeSelectedEndDateAndFilterTypeCategoryAccount() =
        runTest(mainDispatcherRule.testDispatcher) {
            val selectedDay = Calendar.getInstance().apply {
                set(2026, Calendar.AUGUST, 12, 18, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val endOfSelectedDay = Calendar.getInstance().apply {
                timeInMillis = selectedDay
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 30)
            }.timeInMillis
            val repository = FakeBookeeperRepository(
                categories = listOf(category()),
                accounts = listOf(account()),
                transactions = listOf(
                    transaction().copy(id = 1L, occurredAtMillis = endOfSelectedDay),
                    transaction().copy(id = 2L, type = TransactionType.INCOME, occurredAtMillis = endOfSelectedDay),
                ),
            )
            val viewModel = LedgerViewModel(repository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            viewModel.selectType(TransactionType.EXPENSE)
            viewModel.selectCategory(1L)
            viewModel.selectAccount(1L)
            viewModel.selectEndDate(selectedDay)
            advanceUntilIdle()

            assertEquals(listOf(1L), viewModel.uiState.value.items.map { it.id })
            assertEquals(TransactionType.EXPENSE, viewModel.uiState.value.filter.type)
            assertEquals(1L, viewModel.uiState.value.filter.categoryId)
            assertEquals(1L, viewModel.uiState.value.filter.accountId)
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
