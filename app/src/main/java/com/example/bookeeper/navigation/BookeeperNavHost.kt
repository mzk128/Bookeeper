package com.example.bookeeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookeeper.data.repository.BookeeperRepository
import com.example.bookeeper.ui.home.HomeScreen
import com.example.bookeeper.ui.ledger.LedgerScreen
import com.example.bookeeper.ui.ledger.LedgerViewModel
import com.example.bookeeper.ui.settings.SettingsScreen
import com.example.bookeeper.ui.statistics.StatisticsScreen
import com.example.bookeeper.ui.transaction.AddTransactionScreen
import com.example.bookeeper.ui.transaction.AddTransactionViewModel

@Composable
fun BookeeperNavHost(
    navController: NavHostController,
    repository: BookeeperRepository,
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
            val ledgerViewModel: LedgerViewModel = viewModel(
                factory = LedgerViewModel.factory(repository),
            )
            LedgerScreen(
                viewModel = ledgerViewModel,
                onAddTransaction = {
                    navController.navigate(AppDestination.ADD_TRANSACTION) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(TopLevelDestination.STATISTICS.route) {
            StatisticsScreen()
        }
        composable(TopLevelDestination.SETTINGS.route) {
            SettingsScreen()
        }
        composable(AppDestination.ADD_TRANSACTION) {
            val addTransactionViewModel: AddTransactionViewModel = viewModel(
                factory = AddTransactionViewModel.factory(repository),
            )
            AddTransactionScreen(
                viewModel = addTransactionViewModel,
                onSaved = { navController.popBackStack() },
            )
        }
    }
}
