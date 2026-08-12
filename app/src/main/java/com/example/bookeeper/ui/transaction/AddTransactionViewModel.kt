package com.example.bookeeper.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookeeper.data.repository.BookeeperRepository
import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionRecord
import com.example.bookeeper.domain.model.TransactionType
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AmountError {
    REQUIRED,
    INVALID,
    NOT_POSITIVE,
}

enum class DateError {
    FUTURE,
}

data class TransactionFormState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountInput: String = "",
    val selectedCategoryId: Long? = null,
    val selectedAccountId: Long? = null,
    val occurredAtMillis: Long,
    val note: String = "",
    val amountError: AmountError? = null,
    val categoryError: Boolean = false,
    val accountError: Boolean = false,
    val dateError: DateError? = null,
    val noteError: Boolean = false,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
    val savedTransactionId: Long? = null,
)

data class AddTransactionUiState(
    val form: TransactionFormState,
    val categories: List<Category>,
    val accounts: List<Account>,
)

class AddTransactionViewModel(
    private val repository: BookeeperRepository,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val formState = MutableStateFlow(
        TransactionFormState(occurredAtMillis = nowMillis()),
    )

    private val expenseCategories = repository.observeActiveCategories(TransactionType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val incomeCategories = repository.observeActiveCategories(TransactionType.INCOME)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val accounts = repository.observeActiveAccounts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val uiState: StateFlow<AddTransactionUiState> = combine(
        formState,
        expenseCategories,
        incomeCategories,
        accounts,
    ) { form, availableExpenseCategories, availableIncomeCategories, availableAccounts ->
        val availableCategories = when (form.type) {
            TransactionType.EXPENSE -> availableExpenseCategories
            TransactionType.INCOME -> availableIncomeCategories
        }
        val categoryId = form.selectedCategoryId
            ?.takeIf { selected -> availableCategories.any { it.id == selected } }
            ?: availableCategories.firstOrNull()?.id
        val accountId = form.selectedAccountId
            ?.takeIf { selected -> availableAccounts.any { it.id == selected } }
            ?: availableAccounts.firstOrNull()?.id
        val normalizedForm = if (
            categoryId != form.selectedCategoryId || accountId != form.selectedAccountId
        ) {
            form.copy(
                selectedCategoryId = categoryId,
                selectedAccountId = accountId,
            )
        } else {
            form
        }
        AddTransactionUiState(
            form = normalizedForm,
            categories = availableCategories,
            accounts = availableAccounts,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddTransactionUiState(
            form = formState.value,
            categories = emptyList(),
            accounts = emptyList(),
        ),
    )

    fun selectType(type: TransactionType) {
        formState.update {
            it.copy(
                type = type,
                selectedCategoryId = null,
                categoryError = false,
                saveFailed = false,
            )
        }
    }

    fun updateAmount(input: String) {
        if (input.length <= MAX_AMOUNT_INPUT_LENGTH) {
            formState.update { it.copy(amountInput = input, amountError = null, saveFailed = false) }
        }
    }

    fun selectCategory(categoryId: Long) {
        formState.update {
            it.copy(selectedCategoryId = categoryId, categoryError = false, saveFailed = false)
        }
    }

    fun selectAccount(accountId: Long) {
        formState.update {
            it.copy(selectedAccountId = accountId, accountError = false, saveFailed = false)
        }
    }

    fun updateDate(occurredAtMillis: Long) {
        formState.update {
            it.copy(occurredAtMillis = occurredAtMillis, dateError = null, saveFailed = false)
        }
    }

    fun updateNote(note: String) {
        formState.update {
            it.copy(
                note = note,
                noteError = note.length > MAX_NOTE_LENGTH,
                saveFailed = false,
            )
        }
    }

    fun save() {
        val form = formState.value
        if (form.isSaving || form.savedTransactionId != null) return

        val availableCategories = when (form.type) {
            TransactionType.EXPENSE -> expenseCategories.value
            TransactionType.INCOME -> incomeCategories.value
        }
        val availableAccounts = accounts.value
        val categoryId = form.selectedCategoryId
            ?.takeIf { selected -> availableCategories.any { it.id == selected } }
            ?: availableCategories.firstOrNull()?.id
        val accountId = form.selectedAccountId
            ?.takeIf { selected -> availableAccounts.any { it.id == selected } }
            ?: availableAccounts.firstOrNull()?.id

        val amountResult = parseAmountInCents(form.amountInput)
        val amountError = amountResult.exceptionOrNull() as? AmountValidationException
        val categoryValid = categoryId != null
        val accountValid = accountId != null
        val dateError = if (form.occurredAtMillis > nowMillis()) DateError.FUTURE else null
        val noteInvalid = form.note.length > MAX_NOTE_LENGTH

        if (amountError != null || !categoryValid || !accountValid || dateError != null || noteInvalid) {
            formState.update {
                it.copy(
                    amountError = amountError?.error,
                    categoryError = !categoryValid,
                    accountError = !accountValid,
                    dateError = dateError,
                    noteError = noteInvalid,
                )
            }
            return
        }

        val amountInCents = amountResult.getOrThrow()
        val validCategoryId = requireNotNull(categoryId)
        val validAccountId = requireNotNull(accountId)
        formState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            runCatching {
                val now = nowMillis()
                repository.addTransaction(
                    TransactionRecord(
                        id = 0L,
                        type = form.type,
                        amount = Money(amountInCents),
                        categoryId = validCategoryId,
                        accountId = validAccountId,
                        note = form.note.trim(),
                        occurredAtMillis = form.occurredAtMillis,
                        createdAtMillis = now,
                        updatedAtMillis = now,
                    ),
                )
            }.onSuccess { id ->
                formState.update { it.copy(isSaving = false, savedTransactionId = id) }
            }.onFailure {
                formState.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }

    companion object {
        const val MAX_NOTE_LENGTH = 200
        private const val MAX_AMOUNT_INPUT_LENGTH = 18

        fun factory(repository: BookeeperRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AddTransactionViewModel(repository) }
        }

        internal fun parseAmountInCents(input: String): Result<Long> {
            val normalized = input.trim()
            if (normalized.isEmpty()) {
                return Result.failure(AmountValidationException(AmountError.REQUIRED))
            }
            if (!AMOUNT_PATTERN.matches(normalized)) {
                return Result.failure(AmountValidationException(AmountError.INVALID))
            }
            return runCatching {
                BigDecimal(normalized).movePointRight(2).longValueExact()
            }.mapCatching { cents ->
                if (cents <= 0L) {
                    throw AmountValidationException(AmountError.NOT_POSITIVE)
                }
                cents
            }.recoverCatching {
                if (it is AmountValidationException) throw it
                throw AmountValidationException(AmountError.INVALID)
            }
        }

        private val AMOUNT_PATTERN = Regex("^(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?$")
    }

    private class AmountValidationException(val error: AmountError) : IllegalArgumentException()
}
