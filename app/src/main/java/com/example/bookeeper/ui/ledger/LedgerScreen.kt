package com.example.bookeeper.ui.ledger

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.bookeeper.R
import com.example.bookeeper.ui.components.FeaturePlaceholderScreen

@Composable
fun LedgerScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(
        title = stringResource(R.string.ledger_placeholder_title),
        description = stringResource(R.string.ledger_placeholder_description),
        plannedFeatures = listOf(
            stringResource(R.string.ledger_feature_grouping),
            stringResource(R.string.ledger_feature_filter),
            stringResource(R.string.ledger_feature_manage),
        ),
        modifier = modifier,
    )
}
