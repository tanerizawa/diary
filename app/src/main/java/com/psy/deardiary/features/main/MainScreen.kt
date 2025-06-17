package com.psy.deardiary.features.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.psy.deardiary.features.growth.GrowthScreen
import com.psy.deardiary.features.home.HomeScreen
import com.psy.deardiary.features.media.MediaScreen
import com.psy.deardiary.features.services.ServicesScreen
import com.psy.deardiary.navigation.Screen
import java.net.URLEncoder

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

    // ▼▼▼ CHECKPOINT 1: Pastikan rute untuk "Beranda" adalah Screen.Home.route ▼▼▼
    val items = listOf(
        BottomNavItem("Beranda", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Media", Screen.Media.route, Icons.Filled.Movie, Icons.Outlined.Movie),
        BottomNavItem("Layanan", Screen.Services.route, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        BottomNavItem("Pertumbuhan", Screen.Growth.route, Icons.Filled.Yard, Icons.Outlined.Yard)
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
                                popUpTo(localNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(if (currentDestination?.hierarchy?.any { it.route == item.route } == true) item.selectedIcon else item.unselectedIcon, item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = localNavController,
            // ▼▼▼ CHECKPOINT 2: Pastikan startDestination adalah Screen.Home.route ▼▼▼
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ▼▼▼ CHECKPOINT 3: Pastikan composable ini untuk Screen.Home.route dan memanggil HomeScreen ▼▼▼
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSettings = { mainNavController.navigate(Screen.Settings.route) },
                    onNavigateToCrisisSupport = { mainNavController.navigate(Screen.CrisisSupport.route) }
                )
            }
            composable(Screen.Media.route) {
                MediaScreen(
                    onNavigateToEditor = { mainNavController.navigate(Screen.Editor.createRoute(null)) },
                    onNavigateToEditorWithPrompt = { prompt ->
                        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
                        mainNavController.navigate(Screen.Editor.createRoute(prompt = encodedPrompt))
                    }
                )
            }
            composable(Screen.Services.route) {
                ServicesScreen(navController = mainNavController)
            }
            composable(Screen.Growth.route) {
                GrowthScreen()
            }
        }
    }
}