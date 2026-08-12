package com.example.bookeeper.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LedgerContent(
        uiState = uiState,
        onAddTransaction = onAddTransaction,
        modifier = modifier,
    )
}

@Composable
internal fun LedgerContent(
    uiState: LedgerUiState,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.items, key = LedgerItemUiState::id) { item ->
                    LedgerItem(item)
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
private fun LedgerItem(item: LedgerItemUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
