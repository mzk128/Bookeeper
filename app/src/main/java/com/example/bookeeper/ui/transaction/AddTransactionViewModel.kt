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
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
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
    private val transactionId: Long? = null,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private var originalTransaction: TransactionRecord? = null
    private val formState = MutableStateFlow(
        TransactionFormState(
            occurredAtMillis = nowMillis(),
            isLoading = transactionId != null,
        ),
    )

    private val allCategories = repository.observeAllCategories()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allAccounts = repository.observeAllAccounts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val uiState: StateFlow<AddTransactionUiState> = combine(
        formState,
        allCategories,
        allAccounts,
    ) { form, categories, accounts ->
        val availableCategories = categories.filter { category ->
            category.transactionType == form.type &&
                (!category.isArchived || category.id == form.selectedCategoryId)
        }
        val availableAccounts = accounts.filter { account ->
            !account.isArchived || account.id == form.selectedAccountId
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

    init {
        if (transactionId != null) {
            viewModelScope.launch {
                runCatching { repository.getTransaction(transactionId) }
                    .onSuccess { transaction ->
                        originalTransaction = transaction
                        formState.value = if (transaction == null) {
                            formState.value.copy(isLoading = false, loadFailed = true)
                        } else {
                            TransactionFormState(
                                type = transaction.type,
                                amountInput = BigDecimal.valueOf(
                                    transaction.amount.cents,
                                    2,
                                ).stripTrailingZeros().toPlainString(),
                                selectedCategoryId = transaction.categoryId,
                                selectedAccountId = transaction.accountId,
                                occurredAtMillis = transaction.occurredAtMillis,
                                note = transaction.note,
                            )
                        }
                    }
                    .onFailure {
                        formState.update { it.copy(isLoading = false, loadFailed = true) }
                    }
            }
        }
    }

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

        val availableCategories = allCategories.value.filter { category ->
            category.transactionType == form.type &&
                (!category.isArchived || category.id == form.selectedCategoryId)
        }
        val availableAccounts = allAccounts.value.filter { account ->
            !account.isArchived || account.id == form.selectedAccountId
        }
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
                val original = originalTransaction
                val transaction = TransactionRecord(
                        id = original?.id ?: 0L,
                        type = form.type,
                        amount = Money(amountInCents),
                        categoryId = validCategoryId,
                        accountId = validAccountId,
                        note = form.note.trim(),
                        occurredAtMillis = form.occurredAtMillis,
                        createdAtMillis = original?.createdAtMillis ?: now,
                        updatedAtMillis = now,
                    )
                if (original == null) {
                    repository.addTransaction(transaction)
                } else {
                    check(repository.updateTransaction(transaction))
                    transaction.id
                }
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

        fun factory(
            repository: BookeeperRepository,
            transactionId: Long,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { AddTransactionViewModel(repository, transactionId) }
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
