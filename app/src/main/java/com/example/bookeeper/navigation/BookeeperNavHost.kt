package com.example.bookeeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bookeeper.ui.home.HomeScreen
import com.example.bookeeper.ui.ledger.LedgerScreen
import com.example.bookeeper.ui.settings.SettingsScreen
import com.example.bookeeper.ui.statistics.StatisticsScreen

@Composable
fun BookeeperNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TopLevelDestination.HOME.route,
        modifier = modifier,
    ) {
        composable(TopLevelDestination.HOME.route) {
            HomeScreen()
        }
        composable(TopLevelDestination.LEDGER.route) {
            LedgerScreen()
        }
        composable(TopLevelDestination.STATISTICS.route) {
            StatisticsScreen()
        }
        composable(TopLevelDestination.SETTINGS.route) {
            SettingsScreen()
        }
    }
}
