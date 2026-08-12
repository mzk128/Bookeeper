package com.example.bookeeper.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookeeper.R
import com.example.bookeeper.domain.model.Account
import com.example.bookeeper.domain.model.Category
import com.example.bookeeper.domain.model.TransactionType
import java.text.DateFormat
import java.util.Date

@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.form.savedTransactionId) {
        if (uiState.form.savedTransactionId != null) onSaved()
    }

    AddTransactionContent(
        uiState = uiState,
        onTypeSelected = viewModel::selectType,
        onAmountChanged = viewModel::updateAmount,
        onCategorySelected = viewModel::selectCategory,
        onAccountSelected = viewModel::selectAccount,
        onDateSelected = viewModel::updateDate,
        onNoteChanged = viewModel::updateNote,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTransactionContent(
    uiState: AddTransactionUiState,
    onTypeSelected: (TransactionType) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onAccountSelected: (Long) -> Unit,
    onDateSelected: (Long) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val form = uiState.form
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.transaction_type))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = form.type == TransactionType.EXPENSE,
                onClick = { onTypeSelected(TransactionType.EXPENSE) },
                label = { Text(stringResource(R.string.transaction_expense)) },
            )
            FilterChip(
                selected = form.type == TransactionType.INCOME,
                onClick = { onTypeSelected(TransactionType.INCOME) },
                label = { Text(stringResource(R.string.transaction_income)) },
            )
        }

        TextField(
            value = form.amountInput,
            onValueChange = onAmountChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.transaction_amount)) },
            supportingText = form.amountError?.let { error ->
                { Text(stringResource(error.messageResId)) }
            },
            isError = form.amountError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        SelectionDropdown(
            label = stringResource(R.string.transaction_category),
            selectedText = uiState.categories
                .firstOrNull { it.id == form.selectedCategoryId }
                ?.name
                .orEmpty(),
            options = uiState.categories,
            optionName = Category::name,
            optionId = Category::id,
            onSelected = onCategorySelected,
            errorText = if (form.categoryError) {
                stringResource(R.string.transaction_category_required)
            } else {
                null
            },
        )

        SelectionDropdown(
            label = stringResource(R.string.transaction_account),
            selectedText = uiState.accounts
                .firstOrNull { it.id == form.selectedAccountId }
                ?.name
                .orEmpty(),
            options = uiState.accounts,
            optionName = Account::name,
            optionId = Account::id,
            onSelected = onAccountSelected,
            errorText = if (form.accountError) {
                stringResource(R.string.transaction_account_required)
            } else {
                null
            },
        )

        TextField(
            value = formatDate(form.occurredAtMillis),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.transaction_date)) },
            readOnly = true,
            isError = form.dateError != null,
            supportingText = form.dateError?.let {
                { Text(stringResource(R.string.transaction_date_future)) }
            },
            trailingIcon = {
                TextButton(onClick = { showDatePicker = true }) {
                    Text(stringResource(R.string.transaction_select_date))
                }
            },
        )

        TextField(
            value = form.note,
            onValueChange = onNoteChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.transaction_note)) },
            supportingText = {
                Text(
                    if (form.noteError) {
                        stringResource(R.string.transaction_note_too_long)
                    } else {
                        "${form.note.length}/${AddTransactionViewModel.MAX_NOTE_LENGTH}"
                    },
                )
            },
            isError = form.noteError,
            minLines = 2,
            maxLines = 4,
        )

        if (form.saveFailed) {
            Text(stringResource(R.string.transaction_save_failed))
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !form.isSaving,
        ) {
            Text(
                stringResource(
                    if (form.isSaving) R.string.transaction_saving else R.string.transaction_save,
                ),
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = form.occurredAtMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onDateSelected)
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectionDropdown(
    label: String,
    selectedText: String,
    options: List<T>,
    optionName: (T) -> String,
    optionId: (T) -> Long,
    onSelected: (Long) -> Unit,
    errorText: String?,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (options.isNotEmpty()) expanded = it },
    ) {
        TextField(
            value = selectedText,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            label = { Text(label) },
            readOnly = true,
            isError = errorText != null,
            supportingText = errorText?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(optionName(option)) },
                    onClick = {
                        onSelected(optionId(option))
                        expanded = false
                    },
                )
            }
        }
    }
}

private val AmountError.messageResId: Int
    get() = when (this) {
        AmountError.REQUIRED -> R.string.transaction_amount_required
        AmountError.INVALID -> R.string.transaction_amount_invalid
        AmountError.NOT_POSITIVE -> R.string.transaction_amount_positive
    }

private fun formatDate(timeMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timeMillis))
