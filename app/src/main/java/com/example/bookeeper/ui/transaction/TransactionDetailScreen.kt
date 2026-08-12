package com.example.bookeeper.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun TransactionDetailScreen(
    viewModel: TransactionDetailViewModel,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onDeleted()
    }
    TransactionDetailContent(
        uiState = uiState,
        onEdit = onEdit,
        onDelete = viewModel::delete,
        modifier = modifier,
    )
}

@Composable
internal fun TransactionDetailContent(
    uiState: TransactionDetailUiState,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val transaction = uiState.transaction

    Box(modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            transaction == null -> Text(
                stringResource(R.string.transaction_not_found),
                modifier = Modifier.align(Alignment.Center),
            )
            else -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = buildString {
                        append(if (transaction.type == TransactionType.INCOME) "+¥" else "-¥")
                        append(BigDecimal.valueOf(transaction.amount.cents, 2).toPlainString())
                    },
                    style = MaterialTheme.typography.headlineMedium,
                )
                DetailRow(stringResource(R.string.transaction_type), if (transaction.type == TransactionType.INCOME) stringResource(R.string.transaction_income) else stringResource(R.string.transaction_expense))
                DetailRow(stringResource(R.string.transaction_category), uiState.categoryName)
                DetailRow(stringResource(R.string.transaction_account), uiState.accountName)
                DetailRow(stringResource(R.string.transaction_date), DateFormat.getDateInstance().format(Date(transaction.occurredAtMillis)))
                if (transaction.note.isNotBlank()) {
                    DetailRow(stringResource(R.string.transaction_note), transaction.note)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.transaction_edit))
                    }
                    OutlinedButton(
                        onClick = { confirmDelete = true },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isDeleting,
                    ) {
                        Text(if (uiState.isDeleting) stringResource(R.string.transaction_deleting) else stringResource(R.string.transaction_delete))
                    }
                }
                if (uiState.deleteFailed) Text(stringResource(R.string.transaction_delete_failed))
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.transaction_delete_title)) },
            text = { Text(stringResource(R.string.transaction_delete_message)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
