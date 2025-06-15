package com.psy.deardiary.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    // Rute untuk alur Pra-Login
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Rute tunggal yang mewakili seluruh alur setelah login (yang berisi Bottom Bar)
    data object MainAppFlow : Screen("main_app_flow")

    // Rute untuk setiap tab di Bottom Navigation Bar
    data object Diary : Screen("diary")
    data object Media : Screen("media")
    data object Services : Screen("services")
    data object Growth : Screen("growth")

    // Rute untuk layar-layar detail yang bisa diakses dari dalam tab
    data object Settings : Screen("settings")
    data object NotificationSettings : Screen("notification_settings")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object CrisisSupport : Screen("crisis_support")

    // Rute untuk Editor Jurnal dengan argumen opsional
    data object Editor : Screen("editor?entryId={entryId}") {
        const val ENTRY_ID_ARG = "entryId"
        val arguments = listOf(
            navArgument(ENTRY_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )

        fun createRoute(entryId: String?): String {
            return if (entryId != null) "editor?entryId=$entryId" else "editor"
        }
    }
}