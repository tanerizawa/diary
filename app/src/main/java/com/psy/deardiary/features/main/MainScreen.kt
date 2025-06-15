package com.psy.deardiary.features.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.psy.deardiary.features.diary.DiaryScreen
import com.psy.deardiary.features.diary.DiaryViewModel
import com.psy.deardiary.features.growth.GrowthScreen
import com.psy.deardiary.features.media.MediaScreen
import com.psy.deardiary.features.services.ServicesScreen
import com.psy.deardiary.navigation.Screen

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(
    mainNavController: androidx.navigation.NavController
) {
    val localNavController = rememberNavController()

    val items = listOf(
        BottomNavItem(
            label = "Beranda",
            route = Screen.Diary.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            label = "Media",
            route = Screen.Media.route,
            selectedIcon = Icons.Filled.Movie,
            unselectedIcon = Icons.Outlined.Movie
        ),
        BottomNavItem(
            label = "Layanan",
            route = Screen.Services.route,
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder
        ),
        BottomNavItem(
            label = "Pertumbuhan",
            route = Screen.Growth.route,
            selectedIcon = Icons.Filled.Yard,
            unselectedIcon = Icons.Outlined.Yard
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by localNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            localNavController.navigate(item.route) {
                                popUpTo(localNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentDestination?.hierarchy?.any { it.route == item.route } == true) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = localNavController,
            startDestination = Screen.Diary.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Diary.route) {
                val diaryViewModel: DiaryViewModel = hiltViewModel()
                val state by diaryViewModel.uiState.collectAsState()
                DiaryScreen(
                    state = state,
                    onNavigateToEditor = {
                        mainNavController.navigate(Screen.Editor.createRoute(null))
                    },
                    onNavigateToSettings = { mainNavController.navigate(Screen.Settings.route) },
                    onNavigateToCrisisSupport = { mainNavController.navigate(Screen.CrisisSupport.route) },
                    onEntryClick = { entryId ->
                        mainNavController.navigate(Screen.Editor.createRoute(entryId.toString()))
                    },
                    onRetry = { diaryViewModel.refreshJournals() },
                    onClearError = { diaryViewModel.clearErrorMessage() }
                )
            }
            composable(Screen.Media.route) {
                MediaScreen()
            }
            composable(Screen.Services.route) {
                ServicesScreen(onNavigateToCrisisSupport = { mainNavController.navigate(Screen.CrisisSupport.route) })
            }
            composable(Screen.Growth.route) {
                GrowthScreen()
            }
        }
    }
}
