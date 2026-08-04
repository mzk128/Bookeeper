package com.example.bookeeper.ui.statistics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.bookeeper.R
import com.example.bookeeper.ui.components.FeaturePlaceholderScreen

@Composable
fun StatisticsScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(
        title = stringResource(R.string.statistics_placeholder_title),
        description = stringResource(R.string.statistics_placeholder_description),
        plannedFeatures = listOf(
            stringResource(R.string.statistics_feature_monthly),
            stringResource(R.string.statistics_feature_trend),
            stringResource(R.string.statistics_feature_category),
        ),
        modifier = modifier,
    )
}
