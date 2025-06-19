package com.psy.deardiary

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.psy.deardiary.features.auth.AuthViewModel
import com.psy.deardiary.features.auth.LoginScreen
import com.psy.deardiary.features.main.MainViewModel
import com.psy.deardiary.fakes.TestRepositories
import org.junit.Rule
import org.junit.Test

class LoginFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loginNavigatesToMainScreen() {
        val authViewModel = AuthViewModel(TestRepositories.authRepository())
        val mainViewModel = MainViewModel()

        composeRule.setContent {
            var loggedIn by remember { mutableStateOf(false) }
            if (loggedIn) {
                androidx.compose.material3.Text("Main Screen")
            } else {
                LoginScreen(
                    onNavigateToRegister = {},
                    mainViewModel = mainViewModel,
                    authViewModel = authViewModel
                )
                val uiState by authViewModel.uiState.collectAsState()
                if (uiState.isLoginSuccess) {
                    loggedIn = true
                    authViewModel.onAuthEventConsumed()
                }
            }
        }

        composeRule.onNodeWithText("Email").performTextInput("user@example.com")
        composeRule.onNodeWithText("Password").performTextInput("pass")
        composeRule.onNodeWithText("Login").performClick()

        composeRule.onNodeWithText("Main Screen").assertIsDisplayed()
    }
}
