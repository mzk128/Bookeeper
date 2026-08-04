package com.example.bookeeper.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bookeeper.navigation.BookeeperNavHost
import com.example.bookeeper.navigation.TopLevelDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookeeperApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentTopLevelDestination = currentDestination.toTopLevelDestination()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(currentTopLevelDestination.labelResId))
                },
            )
        },
        bottomBar = {
            BookeeperBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = navController::navigateToTopLevelDestination,
            )
        },
    ) { innerPadding ->
        BookeeperNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

@Composable
private fun BookeeperBottomBar(
    currentDestination: NavDestination?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == destination.route
            } == true
            val label = stringResource(destination.labelResId)

            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) {
                            destination.selectedIcon
                        } else {
                            destination.unselectedIcon
                        },
                        contentDescription = label,
                    )
                },
                label = { Text(label) },
            )
        }
    }
}

private fun NavDestination?.toTopLevelDestination(): TopLevelDestination =
    TopLevelDestination.entries.firstOrNull { destination ->
        this?.hierarchy?.any { it.route == destination.route } == true
    } ?: TopLevelDestination.HOME

private fun NavHostController.navigateToTopLevelDestination(
    destination: TopLevelDestination,
) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
