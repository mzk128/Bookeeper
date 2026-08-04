package com.example.bookeeper.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.bookeeper.R
import com.example.bookeeper.ui.components.FeaturePlaceholderScreen

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    FeaturePlaceholderScreen(
        title = stringResource(R.string.settings_placeholder_title),
        description = stringResource(R.string.settings_placeholder_description),
        plannedFeatures = listOf(
            stringResource(R.string.settings_feature_categories),
            stringResource(R.string.settings_feature_preferences),
            stringResource(R.string.settings_feature_backup),
        ),
        modifier = modifier,
    )
}
