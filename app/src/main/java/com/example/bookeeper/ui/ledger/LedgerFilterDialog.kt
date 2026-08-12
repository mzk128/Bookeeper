package com.example.bookeeper.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bookeeper.R
import com.example.bookeeper.domain.model.TransactionType
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LedgerFilterDialog(
    uiState: LedgerUiState,
    onTypeSelected: (TransactionType?) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onAccountSelected: (Long?) -> Unit,
    onStartDateSelected: (Long?) -> Unit,
    onEndDateSelected: (Long?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var dateTarget by remember { mutableStateOf<DateTarget?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ledger_filter_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChip(null, uiState.filter.type, onTypeSelected)
                    TypeChip(TransactionType.EXPENSE, uiState.filter.type, onTypeSelected)
                    TypeChip(TransactionType.INCOME, uiState.filter.type, onTypeSelected)
                }
                FilterDropdown(
                    label = stringResource(R.string.transaction_category),
                    selectedId = uiState.filter.categoryId,
                    options = uiState.categoryOptions.map { it.id to it.name },
                    onSelected = onCategorySelected,
                )
                FilterDropdown(
                    label = stringResource(R.string.transaction_account),
                    selectedId = uiState.filter.accountId,
                    options = uiState.accountOptions.map { it.id to it.name },
                    onSelected = onAccountSelected,
                )
                DateFilterButton(
                    label = stringResource(R.string.ledger_filter_start_date),
                    value = uiState.filter.startDateMillis,
                    onClick = { dateTarget = DateTarget.START },
                    onClear = { onStartDateSelected(null) },
                )
                DateFilterButton(
                    label = stringResource(R.string.ledger_filter_end_date),
                    value = uiState.filter.endDateMillis,
                    onClick = { dateTarget = DateTarget.END },
                    onClear = { onEndDateSelected(null) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ledger_filter_apply)) }
        },
        dismissButton = {
            TextButton(onClick = onClear) { Text(stringResource(R.string.ledger_filter_clear)) }
        },
    )

    dateTarget?.let { target ->
        val current = if (target == DateTarget.START) {
            uiState.filter.startDateMillis
        } else {
            uiState.filter.endDateMillis
        }
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = current)
        DatePickerDialog(
            onDismissRequest = { dateTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    if (target == DateTarget.START) {
                        onStartDateSelected(pickerState.selectedDateMillis)
                    } else {
                        onEndDateSelected(pickerState.selectedDateMillis)
                    }
                    dateTarget = null
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { dateTarget = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) { DatePicker(pickerState) }
    }
}

@Composable
private fun TypeChip(
    type: TransactionType?,
    selected: TransactionType?,
    onSelected: (TransactionType?) -> Unit,
) {
    val label = when (type) {
        null -> stringResource(R.string.ledger_filter_all)
        TransactionType.EXPENSE -> stringResource(R.string.transaction_expense)
        TransactionType.INCOME -> stringResource(R.string.transaction_income)
    }
    FilterChip(selected = selected == type, onClick = { onSelected(type) }, label = { Text(label) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    selectedId: Long?,
    options: List<Pair<Long, String>>,
    onSelected: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.firstOrNull { it.first == selectedId }?.second
        ?: stringResource(R.string.ledger_filter_all)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextField(
            value = selectedText,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.ledger_filter_all)) },
                onClick = { onSelected(null); expanded = false },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.second) },
                    onClick = { onSelected(option.first); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun DateFilterButton(
    label: String,
    value: Long?,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onClick) {
            Text("$label：${value?.let { DateFormat.getDateInstance().format(Date(it)) } ?: stringResource(R.string.ledger_filter_no_limit)}")
        }
        if (value != null) TextButton(onClick = onClear) { Text(stringResource(R.string.action_clear)) }
    }
}

private enum class DateTarget { START, END }
