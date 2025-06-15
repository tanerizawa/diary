// File: app/src/main/java/com/psy/deardiary/navigation/AppNavigation.kt
// Deskripsi: Versi lengkap dan diperbaiki dari file navigasi aplikasi.

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
import com.psy.deardiary.features.settings.NotificationSettingsScreen
import com.psy.deardiary.features.settings.PrivacyPolicyScreen
import com.psy.deardiary.features.settings.SettingsScreen
import com.psy.deardiary.features.services.mbti.MbtiResultScreen
import com.psy.deardiary.features.services.mbti.MbtiTestScreen

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
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val uiState by authViewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isRegisterSuccess) {
                if (uiState.isRegisterSuccess) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) {}
                    }
                    authViewModel.onAuthEventConsumed()
                }
            }

            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- ALUR SETELAH LOGIN ---

        composable(Screen.MainAppFlow.route) {
            MainScreen(mainNavController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToNotification = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onAccountDeleted = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.CrisisSupport.route) {
            CrisisSupportScreen()
        }

        // --- TES MBTI ---

        composable(Screen.MbtiTest.route) {
            MbtiTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onTestComplete = { resultType ->
                    navController.navigate(Screen.MbtiResult.createRoute(resultType)) {
                        popUpTo(Screen.MbtiTest.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.MbtiResult.route,
            arguments = Screen.MbtiResult.arguments
        ) { backStackEntry ->
            val resultType = backStackEntry.arguments?.getString(Screen.MbtiResult.RESULT_TYPE_ARG) ?: ""
            MbtiResultScreen(
                resultType = resultType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- JURNAL EDITOR ---

        composable(
            route = Screen.Editor.route,
            arguments = Screen.Editor.arguments
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString(Screen.Editor.ENTRY_ID_ARG)
            val prompt = backStackEntry.arguments?.getString(Screen.Editor.PROMPT_ARG)
            JournalEditorScreen(
                entryId = entryId,
                prompt = prompt,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
