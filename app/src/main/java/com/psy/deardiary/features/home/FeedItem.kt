package com.psy.deardiary.features.home

import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.features.media.Article

sealed interface FeedItem {
    data class WelcomeItem(val timeOfDay: String, val userName: String) : FeedItem
    data class PromptItem(val promptText: String) : FeedItem
    data class JournalItem(val journalEntry: JournalEntry) : FeedItem
    data class ArticleSuggestionItem(val article: Article) : FeedItem
}