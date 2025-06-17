package com.psy.deardiary.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder

sealed class Screen(val route: String) {

    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object MainAppFlow : Screen("main_app_flow")
    object Home : Screen("home")
    object Media : Screen("media")
    object Services : Screen("services")
    object Growth : Screen("growth")
    object Settings : Screen("settings")
    object NotificationSettings : Screen("notification_settings")
    object PrivacyPolicy : Screen("privacy_policy")
    object CrisisSupport : Screen("crisis_support")
    object EmergencyContactSettings : Screen("emergency_contact_settings")

    object Editor : Screen("editor") {
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
            val builder = StringBuilder(route)
            var first = true

            fun appendArg(key: String, value: String?) {
                if (value != null) {
                    builder.append(if (first) '?' else '&')
                        .append(key)
                        .append('=')
                        .append(URLEncoder.encode(value, "UTF-8"))
                    first = false
                }
            }

            appendArg(ENTRY_ID_ARG, entryId)
            appendArg(PROMPT_ARG, prompt)
            return builder.toString()
        }
    }

    object MbtiTest : Screen("mbti_test")
    object MbtiResult : Screen("mbti_result/{resultType}") {
        const val RESULT_TYPE_ARG = "resultType"
        val arguments = listOf(
            navArgument(RESULT_TYPE_ARG) { type = NavType.StringType }
        )
        fun createRoute(resultType: String) = "mbti_result/$resultType"
    }

    object DassTest : Screen("dass_test")
    object DassResult : Screen(
        "dass_result/{depressionScore}/{anxietyScore}/{stressScore}"
    ) {
        const val DEPRESSION_ARG = "depressionScore"
        const val ANXIETY_ARG = "anxietyScore"
        const val STRESS_ARG = "stressScore"

        val arguments = listOf(
            navArgument(DEPRESSION_ARG) { type = NavType.IntType },
            navArgument(ANXIETY_ARG)   { type = NavType.IntType },
            navArgument(STRESS_ARG)    { type = NavType.IntType }
        )

        fun createRoute(depression: Int, anxiety: Int, stress: Int) =
            "dass_result/$depression/$anxiety/$stress"
    }
}
