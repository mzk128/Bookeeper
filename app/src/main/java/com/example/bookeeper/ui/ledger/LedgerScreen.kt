package com.example.bookeeper.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookeeper.R
import com.example.bookeeper.domain.model.TransactionType
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Date

@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel,
    onAddTransaction: () -> Unit,
    onTransactionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    LedgerContent(
        uiState = uiState,
        onAddTransaction = onAddTransaction,
        onTransactionSelected = onTransactionSelected,
        onShowFilters = { showFilters = true },
        modifier = modifier,
    )
    if (showFilters) {
        LedgerFilterDialog(
            uiState = uiState,
            onTypeSelected = viewModel::selectType,
            onCategorySelected = viewModel::selectCategory,
            onAccountSelected = viewModel::selectAccount,
            onStartDateSelected = viewModel::selectStartDate,
            onEndDateSelected = viewModel::selectEndDate,
            onClear = viewModel::clearFilters,
            onDismiss = { showFilters = false },
        )
    }
}

@Composable
internal fun LedgerContent(
    uiState: LedgerUiState,
    onAddTransaction: () -> Unit,
    onTransactionSelected: (Long) -> Unit = {},
    onShowFilters: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        TextButton(
            onClick = onShowFilters,
            modifier = Modifier.align(Alignment.TopEnd).padding(horizontal = 12.dp),
        ) {
            Text(
                if (uiState.filter.isActive) {
                    stringResource(R.string.ledger_filter_active)
                } else {
                    stringResource(R.string.ledger_filter)
                },
            )
        }
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            uiState.items.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(R.string.ledger_empty_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(stringResource(R.string.ledger_empty_description))
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    top = 56.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.items, key = LedgerItemUiState::id) { item ->
                    LedgerItem(item, onClick = { onTransactionSelected(item.id) })
                }
            }
        }

        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.ledger_add_transaction),
            )
        }
    }
}

@Composable
private fun LedgerItem(item: LedgerItemUiState, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(item.categoryName, style = MaterialTheme.typography.titleMedium)
                Text("${item.accountName} · ${formatDate(item.occurredAtMillis)}")
                if (item.note.isNotBlank()) Text(item.note)
            }
            Text(
                text = buildString {
                    append(if (item.type == TransactionType.INCOME) "+¥" else "-¥")
                    append(BigDecimal.valueOf(item.amount.cents, 2).toPlainString())
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (item.type == TransactionType.INCOME) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

private fun formatDate(timeMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timeMillis))
