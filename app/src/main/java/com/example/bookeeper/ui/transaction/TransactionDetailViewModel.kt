package com.example.bookeeper.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookeeper.data.repository.BookeeperRepository
import com.example.bookeeper.domain.model.TransactionRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val transaction: TransactionRecord? = null,
    val categoryName: String = "",
    val accountName: String = "",
    val isDeleting: Boolean = false,
    val deleteFailed: Boolean = false,
    val deleted: Boolean = false,
)

class TransactionDetailViewModel(
    private val repository: BookeeperRepository,
    private val transactionId: Long,
) : ViewModel() {
    private val operationState = MutableStateFlow(TransactionDetailUiState())

    val uiState: StateFlow<TransactionDetailUiState> = combine(
        repository.observeTransaction(transactionId),
        repository.observeAllCategories(),
        repository.observeAllAccounts(),
        operationState,
    ) { transaction, categories, accounts, operation ->
        operation.copy(
            isLoading = false,
            transaction = transaction,
            categoryName = categories.firstOrNull { it.id == transaction?.categoryId }?.name.orEmpty(),
            accountName = accounts.firstOrNull { it.id == transaction?.accountId }?.name.orEmpty(),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TransactionDetailUiState(),
    )

    fun delete() {
        val transaction = uiState.value.transaction ?: return
        if (uiState.value.isDeleting) return
        operationState.update { it.copy(isDeleting = true, deleteFailed = false) }
        viewModelScope.launch {
            runCatching { repository.deleteTransaction(transaction) }
                .onSuccess { deleted ->
                    operationState.update {
                        it.copy(isDeleting = false, deleted = deleted, deleteFailed = !deleted)
                    }
                }
                .onFailure {
                    operationState.update { it.copy(isDeleting = false, deleteFailed = true) }
                }
        }
    }

    companion object {
        fun factory(
            repository: BookeeperRepository,
            transactionId: Long,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { TransactionDetailViewModel(repository, transactionId) }
        }
    }
}
