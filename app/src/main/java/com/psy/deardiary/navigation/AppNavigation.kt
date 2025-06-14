package com.psy.deardiary.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.psy.deardiary.features.auth.AuthViewModel
import com.psy.deardiary.features.auth.LoginScreen
import com.psy.deardiary.features.auth.RegisterScreen
import com.psy.deardiary.features.crisis_support.CrisisSupportScreen
import com.psy.deardiary.features.diary.JournalEditorScreen
import com.psy.deardiary.features.main.MainScreen
import com.psy.deardiary.features.onboarding.OnboardingScreen
import com.psy.deardiary.features.settings.SettingsScreen
import com.psy.deardiary.features.media.MediaScreen // Import MediaScreen
import com.psy.deardiary.features.services.ServicesScreen // Import ServicesScreen
import com.psy.deardiary.features.growth.GrowthScreen // Import GrowthScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {
        // --- ALUR PRA-LOGIN ---

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val uiState by authViewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isLoginSuccess) {
                if (uiState.isLoginSuccess) {
                    navController.navigate(Screen.MainAppFlow.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    authViewModel.onAuthEventConsumed()
                }
            }

            LoginScreen(
                onLoginClick = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val uiState by authViewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isRegisterSuccess) {
                if (uiState.isRegisterSuccess) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                    authViewModel.onAuthEventConsumed()
                }
            }

            RegisterScreen(
                onRegisterClick = { email, password -> authViewModel.register(email, password) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                isLoading = uiState.isLoading
            )
        }

        // --- ALUR SETELAH LOGIN (APLIKASI UTAMA) ---

        composable(Screen.MainAppFlow.route) {
            MainScreen(mainNavController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onExportData = { /* TODO: Panggil ViewModel */ },
                onDeleteAccount = { /* TODO: Panggil ViewModel */ }
            )
        }

        composable(Screen.CrisisSupport.route) {
            CrisisSupportScreen()
        }

        composable(
            route = Screen.Editor.route,
            arguments = Screen.Editor.arguments
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString(Screen.Editor.ENTRY_ID_ARG)?.toIntOrNull()

            JournalEditorScreen(
                entryId = entryId, // Lewatkan entryId ke JournalEditorScreen
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}