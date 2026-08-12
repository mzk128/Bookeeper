package com.example.bookeeper.ui.transaction

import com.example.bookeeper.MainDispatcherRule
import com.example.bookeeper.data.repository.FakeBookeeperRepository
import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.AccountType
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.Money
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeBookeeperRepository
    private lateinit var viewModel: AddTransactionViewModel

    @Before
    fun setUp() {
        repository = FakeBookeeperRepository(
            categories = listOf(
                category(1L, "餐饮", TransactionType.EXPENSE),
                category(101L, "工资", TransactionType.INCOME),
            ),
            accounts = listOf(account(1L, "现金")),
        )
        viewModel = AddTransactionViewModel(repository, nowMillis = { NOW })
    }

    @Test
    fun amountParser_convertsYuanToExactCents() {
        assertEquals(2_568L, AddTransactionViewModel.parseAmountInCents("25.68").getOrThrow())
        assertEquals(2_500L, AddTransactionViewModel.parseAmountInCents("25").getOrThrow())
        assertTrue(AddTransactionViewModel.parseAmountInCents("1.234").isFailure)
        assertTrue(AddTransactionViewModel.parseAmountInCents("0").isFailure)
    }

    @Test
    fun save_withValidForm_persistsExactTransactionAndPublishesSavedId() =
        runTest(mainDispatcherRule.testDispatcher) {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            viewModel.updateAmount("25.68")
            viewModel.updateNote("午餐")
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.lastAddedTransaction
            assertEquals(Money(2_568L), saved?.amount)
            assertEquals(TransactionType.EXPENSE, saved?.type)
            assertEquals(1L, saved?.categoryId)
            assertEquals(1L, saved?.accountId)
            assertEquals("午餐", saved?.note)
            assertEquals(NOW, saved?.occurredAtMillis)
            assertEquals(1L, viewModel.uiState.value.form.savedTransactionId)
        }

    @Test
    fun save_withMissingAmountAndFutureDate_exposesValidationAndDoesNotPersist() =
        runTest(mainDispatcherRule.testDispatcher) {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            viewModel.updateDate(NOW + 1L)
            viewModel.save()
            advanceUntilIdle()

            val form = viewModel.uiState.value.form
            assertEquals(AmountError.REQUIRED, form.amountError)
            assertEquals(DateError.FUTURE, form.dateError)
            assertNull(repository.lastAddedTransaction)
        }

    @Test
    fun selectingIncome_switchesAvailableCategoriesAndResetsSelection() =
        runTest(mainDispatcherRule.testDispatcher) {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect()
            }
            advanceUntilIdle()

            viewModel.selectType(TransactionType.INCOME)
            advanceUntilIdle()

            assertEquals(listOf("工资"), viewModel.uiState.value.categories.map { it.name })
            assertEquals(101L, viewModel.uiState.value.form.selectedCategoryId)
        }

    private fun category(id: Long, name: String, type: TransactionType) = Category(
        id = id,
        name = name,
        transactionType = type,
        iconKey = "test",
        colorArgb = 0L,
        isBuiltIn = true,
        isArchived = false,
        sortOrder = 0,
        createdAtMillis = 0L,
        updatedAtMillis = 0L,
    )

    private fun account(id: Long, name: String) = Account(
        id = id,
        name = name,
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

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
