package com.example.bookeeper.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookeeper.data.repository.BookeeperRepository
import com.example.bookeeper.ui.home.HomeScreen
import com.example.bookeeper.ui.home.HomeViewModel
import com.example.bookeeper.ui.ledger.LedgerScreen
import com.example.bookeeper.ui.ledger.LedgerViewModel
import com.example.bookeeper.ui.settings.SettingsScreen
import com.example.bookeeper.ui.statistics.StatisticsScreen
import com.example.bookeeper.ui.transaction.AddTransactionScreen
import com.example.bookeeper.ui.transaction.AddTransactionViewModel
import com.example.bookeeper.ui.transaction.TransactionDetailScreen
import com.example.bookeeper.ui.transaction.TransactionDetailViewModel

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
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
            HomeScreen(
                viewModel = homeViewModel,
                onAddTransaction = { navController.navigate(AppDestination.ADD_TRANSACTION) },
                onTransactionSelected = { id -> navController.navigate(AppDestination.transactionDetail(id)) },
            )
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
                onTransactionSelected = { id ->
                    navController.navigate(AppDestination.transactionDetail(id))
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
        composable(
            route = AppDestination.TRANSACTION_DETAIL_PATTERN,
            arguments = listOf(
                navArgument(AppDestination.TRANSACTION_ID_ARGUMENT) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val transactionId = requireNotNull(
                backStackEntry.arguments?.getLong(AppDestination.TRANSACTION_ID_ARGUMENT),
            )
            val detailViewModel: TransactionDetailViewModel = viewModel(
                factory = TransactionDetailViewModel.factory(repository, transactionId),
            )
            TransactionDetailScreen(
                viewModel = detailViewModel,
                onEdit = { navController.navigate(AppDestination.editTransaction(transactionId)) },
                onDeleted = { navController.popBackStack() },
            )
        }
        composable(
            route = AppDestination.EDIT_TRANSACTION_PATTERN,
            arguments = listOf(
                navArgument(AppDestination.TRANSACTION_ID_ARGUMENT) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val transactionId = requireNotNull(
                backStackEntry.arguments?.getLong(AppDestination.TRANSACTION_ID_ARGUMENT),
            )
            val editViewModel: AddTransactionViewModel = viewModel(
                factory = AddTransactionViewModel.factory(repository, transactionId),
            )
            AddTransactionScreen(
                viewModel = editViewModel,
                onSaved = { navController.popBackStack() },
            )
        }
    }
}
