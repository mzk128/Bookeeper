package com.example.bookeeper.ui.home

import com.example.bookeeper.MainDispatcherRule
import com.example.bookeeper.data.repository.FakeBookeeperRepository
import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.AccountType
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionRecord
import com.example.bookeeper.domain.model.TransactionType
import java.util.Calendar
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
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun currentMonthSummaryAndRecentNames_areMappedFromRepositoryFlows() =
        runTest(mainDispatcherRule.testDispatcher) {
            val now = timestamp(2026, Calendar.AUGUST, 12)
            val repository = FakeBookeeperRepository(
                categories = listOf(
                    Category(1L, "餐饮", TransactionType.EXPENSE, "x", 0L, true, false, 0, 0L, 0L),
                    Category(101L, "工资", TransactionType.INCOME, "x", 0L, true, false, 0, 0L, 0L),
                ),
                accounts = listOf(Account(1L, "现金", AccountType.CASH, Money.Zero, "x", 0L, true, false, 0, 0L, 0L)),
                transactions = listOf(
                    transaction(1L, TransactionType.EXPENSE, 2_500L, 1L, timestamp(2026, Calendar.AUGUST, 2)),
                    transaction(2L, TransactionType.INCOME, 10_000L, 101L, timestamp(2026, Calendar.AUGUST, 1)),
                    transaction(3L, TransactionType.INCOME, 99_999L, 101L, timestamp(2026, Calendar.JULY, 31)),
                ),
            )
            val viewModel = HomeViewModel(repository) { now }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(Money(10_000L), state.income)
            assertEquals(Money(2_500L), state.expense)
            assertEquals(Money(7_500L), state.balance)
            assertEquals("餐饮", state.recentItems.first().categoryName)
            assertEquals("现金", state.recentItems.first().accountName)
        }

    private fun timestamp(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 12, 0, 0)
        }.timeInMillis

    private fun transaction(
        id: Long,
        type: TransactionType,
        amount: Long,
        categoryId: Long,
        occurredAt: Long,
    ) = TransactionRecord(id, type, Money(amount), categoryId, 1L, "", occurredAt, occurredAt, occurredAt)
}
