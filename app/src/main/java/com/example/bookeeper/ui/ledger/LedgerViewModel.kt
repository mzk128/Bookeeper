package com.example.bookeeper.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookeeper.data.repository.BookeeperRepository
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionFilter
import com.example.bookeeper.domain.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class LedgerItemUiState(
    val id: Long,
    val type: TransactionType,
    val amount: Money,
    val categoryName: String,
    val accountName: String,
    val note: String,
    val occurredAtMillis: Long,
)

data class LedgerUiState(
    val isLoading: Boolean = true,
    val items: List<LedgerItemUiState> = emptyList(),
)

class LedgerViewModel(repository: BookeeperRepository) : ViewModel() {
    val uiState: StateFlow<LedgerUiState> = combine(
        repository.observeTransactions(
            TransactionFilter(startMillis = 0L, endExclusiveMillis = Long.MAX_VALUE),
        ),
        repository.observeAllCategories(),
        repository.observeAllAccounts(),
    ) { transactions, categories, accounts ->
        val categoryNames = categories.associate { it.id to it.name }
        val accountNames = accounts.associate { it.id to it.name }
        LedgerUiState(
            isLoading = false,
            items = transactions.map { transaction ->
                LedgerItemUiState(
                    id = transaction.id,
                    type = transaction.type,
                    amount = transaction.amount,
                    categoryName = categoryNames[transaction.categoryId] ?: "未知分类",
                    accountName = accountNames[transaction.accountId] ?: "未知账户",
                    note = transaction.note,
                    occurredAtMillis = transaction.occurredAtMillis,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LedgerUiState(),
    )

    companion object {
        fun factory(repository: BookeeperRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { LedgerViewModel(repository) }
        }
    }
}
