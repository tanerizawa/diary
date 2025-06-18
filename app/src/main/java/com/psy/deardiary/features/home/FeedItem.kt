package com.psy.deardiary.features.home

import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.features.media.Article

sealed interface FeedItem {
    data class JournalItem(val journalEntry: JournalEntry) : FeedItem
    data class ArticleSuggestionItem(val article: Article) : FeedItem
}
