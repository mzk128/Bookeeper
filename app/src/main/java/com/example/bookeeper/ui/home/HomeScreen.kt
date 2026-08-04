package com.example.bookeeper.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.bookeeper.R
import com.example.bookeeper.ui.components.FeaturePlaceholderScreen

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(
        title = stringResource(R.string.home_placeholder_title),
        description = stringResource(R.string.home_placeholder_description),
        plannedFeatures = listOf(
            stringResource(R.string.home_feature_summary),
            stringResource(R.string.home_feature_recent),
            stringResource(R.string.home_feature_quick_add),
        ),
        modifier = modifier,
    )
}
