package com.psy.deardiary.features.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.psy.deardiary.data.model.ChatMessage
import org.junit.Rule
import org.junit.Test

class ChatBubbleTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsSentimentInfo_whenPresent() {
        val msg = ChatMessage(
            id = 1,
            text = "hi",
            isUser = false,
            userId = 1,
            sentimentScore = 0.6f,
            keyEmotions = "joy"
        )
        composeTestRule.setContent {
            MaterialTheme {
                ChatBubble(message = msg)
            }
        }
        composeTestRule.onNodeWithText("joy").assertIsDisplayed()
        composeTestRule.onNodeWithText("0.6").assertIsDisplayed()
    }
}
