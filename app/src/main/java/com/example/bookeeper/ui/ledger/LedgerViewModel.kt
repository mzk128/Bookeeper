package com.example.bookeeper.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookeeper.data.repository.BookeeperRepository
import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionFilter
import com.example.bookeeper.domain.model.TransactionType
import com.example.bookeeper.util.startOfDay
import com.example.bookeeper.util.startOfNextDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class LedgerItemUiState(
    val id: Long,
    val type: TransactionType,
    val amount: Money,
    val categoryName: String,
    val accountName: String,
    val note: String,
    val occurredAtMillis: Long,
)

data class LedgerFilterState(
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
) {
    val isActive: Boolean
        get() = type != null || categoryId != null || accountId != null ||
            startDateMillis != null || endDateMillis != null
}

data class LedgerUiState(
    val isLoading: Boolean = true,
    val items: List<LedgerItemUiState> = emptyList(),
    val filter: LedgerFilterState = LedgerFilterState(),
    val categoryOptions: List<Category> = emptyList(),
    val accountOptions: List<Account> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class LedgerViewModel(private val repository: BookeeperRepository) : ViewModel() {
    private val filterState = MutableStateFlow(LedgerFilterState())
    private val categories = repository.observeAllCategories()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val accounts = repository.observeAllAccounts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val transactions = filterState.flatMapLatest { filter ->
        repository.observeTransactions(filter.toTransactionFilter())
    }

    val uiState: StateFlow<LedgerUiState> = combine(
        transactions,
        categories,
        accounts,
        filterState,
    ) { transactionRecords, allCategories, allAccounts, filter ->
        val categoryNames = allCategories.associate { it.id to it.name }
        val accountNames = allAccounts.associate { it.id to it.name }
        LedgerUiState(
            isLoading = false,
            items = transactionRecords.map { transaction ->
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
            filter = filter,
            categoryOptions = allCategories.filter { category ->
                !category.isArchived && (filter.type == null || category.transactionType == filter.type)
            },
            accountOptions = allAccounts.filterNot(Account::isArchived),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LedgerUiState(),
    )

    fun selectType(type: TransactionType?) {
        filterState.update { current ->
            current.copy(
                type = type,
                categoryId = current.categoryId?.takeIf { id ->
                    type == null || categories.value.any {
                        it.id == id && it.transactionType == type
                    }
                },
            )
        }
    }

    fun selectCategory(categoryId: Long?) {
        filterState.update { it.copy(categoryId = categoryId) }
    }

    fun selectAccount(accountId: Long?) {
        filterState.update { it.copy(accountId = accountId) }
    }

    fun selectStartDate(timeMillis: Long?) {
        filterState.update { current ->
            val start = timeMillis?.let(::startOfDay)
            current.copy(
                startDateMillis = start,
                endDateMillis = current.endDateMillis?.takeIf { end -> start == null || end >= start },
            )
        }
    }

    fun selectEndDate(timeMillis: Long?) {
        filterState.update { current ->
            val end = timeMillis?.let(::startOfDay)
            current.copy(
                endDateMillis = end,
                startDateMillis = current.startDateMillis?.takeIf { start -> end == null || start <= end },
            )
        }
    }

    fun clearFilters() {
        filterState.value = LedgerFilterState()
    }

    private fun LedgerFilterState.toTransactionFilter() = TransactionFilter(
        startMillis = startDateMillis ?: 0L,
        endExclusiveMillis = endDateMillis?.let(::startOfNextDay) ?: Long.MAX_VALUE,
        type = type,
        categoryId = categoryId,
        accountId = accountId,
    )

    companion object {
        fun factory(repository: BookeeperRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { LedgerViewModel(repository) }
        }
    }
}
