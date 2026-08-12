package com.example.bookeeper.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookeeper.data.repository.BookeeperRepository
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionType
import com.example.bookeeper.util.monthRangeContaining
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeRecentItemUiState(
    val id: Long,
    val type: TransactionType,
    val amount: Money,
    val categoryName: String,
    val accountName: String,
    val occurredAtMillis: Long,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val income: Money = Money.Zero,
    val expense: Money = Money.Zero,
    val balance: Money = Money.Zero,
    val recentItems: List<HomeRecentItemUiState> = emptyList(),
)

class HomeViewModel(
    repository: BookeeperRepository,
    nowMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val monthRange = monthRangeContaining(nowMillis())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observePeriodSummary(monthRange.startMillis, monthRange.endExclusiveMillis),
        repository.observeRecentTransactions(5),
        repository.observeAllCategories(),
        repository.observeAllAccounts(),
    ) { summary, transactions, categories, accounts ->
        val categoryNames = categories.associate { it.id to it.name }
        val accountNames = accounts.associate { it.id to it.name }
        HomeUiState(
            isLoading = false,
            income = summary.income,
            expense = summary.expense,
            balance = summary.balance,
            recentItems = transactions.map { transaction ->
                HomeRecentItemUiState(
                    id = transaction.id,
                    type = transaction.type,
                    amount = transaction.amount,
                    categoryName = categoryNames[transaction.categoryId] ?: "未知分类",
                    accountName = accountNames[transaction.accountId] ?: "未知账户",
                    occurredAtMillis = transaction.occurredAtMillis,
                )
            },
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(),
    )

    companion object {
        fun factory(repository: BookeeperRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
