package com.psy.deardiary.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.psy.deardiary.features.auth.*
import com.psy.deardiary.features.crisis_support.CrisisSupportScreen
import com.psy.deardiary.features.diary.JournalEditorScreen
import com.psy.deardiary.features.main.MainScreen
import com.psy.deardiary.features.main.MainViewModel
import com.psy.deardiary.features.onboarding.OnboardingScreen
import com.psy.deardiary.features.services.dass.DassResultScreen
import com.psy.deardiary.features.services.dass.DassTestScreen
import com.psy.deardiary.features.services.mbti.MbtiResultScreen
import com.psy.deardiary.features.services.mbti.MbtiTestScreen
import com.psy.deardiary.features.settings.*
import com.psy.deardiary.features.profile.ProfileScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Onboarding.route
    ) {

        // --- PRA-LOGIN ---

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

            val mainViewModel: MainViewModel = hiltViewModel()
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                mainViewModel = mainViewModel
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

            val mainViewModel: MainViewModel = hiltViewModel()
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
                mainViewModel = mainViewModel
            )
        }

        // --- UTAMA ---

        composable(Screen.MainAppFlow.route) {
            val mainViewModel: MainViewModel = hiltViewModel()
            MainScreen(mainNavController = navController, mainViewModel = mainViewModel)
        }

        // --- JURNAL ---

        composable(
            route = Screen.Editor.route,
            arguments = Screen.Editor.arguments
        ) {
            val mainViewModel: MainViewModel = hiltViewModel()
            JournalEditorScreen(
                onBackClick = { navController.popBackStack() },
                mainViewModel = mainViewModel
            )
        }

        // --- LAYANAN: MBTI ---

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

        // --- LAYANAN: DASS ---

        composable(Screen.DassTest.route) {
            DassTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onTestComplete = { depression, anxiety, stress ->
                    navController.navigate(
                        Screen.DassResult.createRoute(depression, anxiety, stress)
                    ) {
                        popUpTo(Screen.DassTest.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.DassResult.route,
            arguments = Screen.DassResult.arguments
        ) { backStackEntry ->
            val depression = backStackEntry.arguments?.getInt(Screen.DassResult.DEPRESSION_ARG) ?: 0
            val anxiety = backStackEntry.arguments?.getInt(Screen.DassResult.ANXIETY_ARG) ?: 0
            val stress = backStackEntry.arguments?.getInt(Screen.DassResult.STRESS_ARG) ?: 0

            DassResultScreen(
                depressionScore = depression,
                anxietyScore = anxiety,
                stressScore = stress,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- SETELAN DAN DUKUNGAN ---

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToNotification = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToEmergencyContact = { navController.navigate(Screen.EmergencyContactSettings.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
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

        composable(Screen.EmergencyContactSettings.route) {
            EmergencyContactScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onBackClick = { navController.popBackStack() })
        }

        // --- BANTUAN KRISIS ---

        composable(Screen.CrisisSupport.route) {
            CrisisSupportScreen(navController = navController)
        }
    }
}
