package com.example.bookeeper.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.bookeeper.domain.model.Money
import com.example.bookeeper.domain.model.TransactionType
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddTransaction: () -> Unit,
    onTransactionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isLoading) {
        androidx.compose.foundation.layout.Box(modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(stringResource(R.string.home_month_summary), style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCard(stringResource(R.string.home_month_income), uiState.income, Modifier.weight(1f))
                SummaryCard(stringResource(R.string.home_month_expense), uiState.expense, Modifier.weight(1f))
                SummaryCard(stringResource(R.string.home_month_balance), uiState.balance, Modifier.weight(1f))
            }
        }
        item {
            Button(onClick = onAddTransaction, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.home_add_transaction))
            }
        }
        item {
            Text(stringResource(R.string.home_recent_transactions), style = MaterialTheme.typography.titleLarge)
        }
        if (uiState.recentItems.isEmpty()) {
            item { Text(stringResource(R.string.home_no_transactions)) }
        } else {
            items(uiState.recentItems, key = HomeRecentItemUiState::id) { item ->
                Card(Modifier.fillMaxWidth().clickable { onTransactionSelected(item.id) }) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(item.categoryName, style = MaterialTheme.typography.titleMedium)
                            Text("${item.accountName} · ${DateFormat.getDateInstance().format(Date(item.occurredAtMillis))}")
                        }
                        Text(
                            (if (item.type == TransactionType.INCOME) "+¥" else "-¥") + formatMoney(item.amount),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, amount: Money, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("¥${formatMoney(amount)}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun formatMoney(money: Money): String =
    BigDecimal.valueOf(money.cents, 2).toPlainString()
