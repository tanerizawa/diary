// File: app/src/main/java/com/psy/deardiary/navigation/Screen.kt
// VERSI DIPERBARUI: Menambahkan argumen 'prompt' ke rute Editor.

package com.psy.deardiary.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder

sealed class Screen(val route: String) {

    // Rute untuk alur Pra-Login
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Rute tunggal untuk seluruh alur setelah login (Bottom Bar)
    data object MainAppFlow : Screen("main_app_flow")

    // Rute untuk setiap tab di Bottom Navigation Bar
    data object Diary : Screen("diary")
    data object Media : Screen("media")
    data object Services : Screen("services")
    data object Growth : Screen("growth")

    // Rute untuk layar-layar detail
    data object Settings : Screen("settings")
    data object NotificationSettings : Screen("notification_settings")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object CrisisSupport : Screen("crisis_support")

    // Rute untuk Editor Jurnal dengan argumen opsional
    data object Editor : Screen("editor?entryId={entryId}&prompt={prompt}") {
        const val ENTRY_ID_ARG = "entryId"
        const val PROMPT_ARG = "prompt"

        val arguments = listOf(
            navArgument(ENTRY_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PROMPT_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )

        fun createRoute(entryId: String? = null, prompt: String? = null): String {
            val routeBuilder = StringBuilder("editor")
            var firstArg = true

            fun appendArg(key: String, value: String?) {
                if (value != null) {
                    routeBuilder.append(if (firstArg) "?" else "&")
                    val encodedValue = URLEncoder.encode(value, "UTF-8")
                    routeBuilder.append("$key=$encodedValue")
                    firstArg = false
                }
            }

            appendArg(ENTRY_ID_ARG, entryId)
            appendArg(PROMPT_ARG, prompt)

            return routeBuilder.toString()
        }
    }

    // Rute untuk Tes Psikologi MBTI
    data object MbtiTest : Screen("mbti_test")
    data object MbtiResult : Screen("mbti_result/{resultType}") {
        const val RESULT_TYPE_ARG = "resultType"

        val arguments = listOf(
            navArgument(RESULT_TYPE_ARG) {
                type = NavType.StringType
            }
        )

        fun createRoute(resultType: String): String {
            return "mbti_result/$resultType"
        }
    }
}
