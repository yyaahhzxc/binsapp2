package com.example.bins_app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bins_app.ui.screens.DashboardScreen
import com.example.bins_app.ui.screens.HistoryScreen
import com.example.bins_app.ui.screens.PeopleScreen
import com.example.bins_app.ui.screens.PersonDetailScreen
import com.example.bins_app.ui.screens.SettingsScreen
import com.example.bins_app.viewmodel.AppViewModel
import com.example.bins_app.viewmodel.AppViewModelProvider

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object People : Screen("people", "People", Icons.Default.People)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object PersonDetail : Screen("person/{friendId}", "Person", Icons.Default.People) {
        fun createRoute(friendId: Int) = "person/$friendId"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VinceAppMainScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: AppViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.People,
        Screen.History,
        Screen.Settings
    )

    // Determine if we should show bottom navigation
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    // Get current screen title
    val currentScreen = bottomNavItems.find {
        currentDestination?.hierarchy?.any { dest -> dest.route == it.route } == true
    } ?: Screen.Home

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Vince's App",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToPerson = { friendId ->
                        navController.navigate(Screen.PersonDetail.createRoute(friendId))
                    }
                )
            }

            composable(Screen.People.route) {
                PeopleScreen(
                    viewModel = viewModel,
                    onNavigateToPerson = { friendId ->
                        navController.navigate(Screen.PersonDetail.createRoute(friendId))
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToTransaction = { transactionId ->
                        // Optional: Navigate to transaction detail
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }

            composable(
                route = Screen.PersonDetail.route,
                arguments = listOf(
                    navArgument("friendId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val friendId = backStackEntry.arguments?.getInt("friendId") ?: 0
                PersonDetailScreen(
                    friendId = friendId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}

