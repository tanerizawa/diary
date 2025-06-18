package com.psy.deardiary.data.dto

import com.psy.deardiary.features.home.FeedItem
import com.psy.deardiary.features.media.Article
import com.psy.deardiary.data.dto.toJournalEntry

fun ArticleResponse.toArticle(): Article {
    return Article(title = title, source = source, imageUrl = imageUrl)
}

fun FeedItemResponse.toFeedItem(): FeedItem? {
    return when (type) {
        "journal" -> journal?.toJournalEntry()?.let { FeedItem.JournalItem(it) }
        "article_suggestion" -> article?.toArticle()?.let { FeedItem.ArticleSuggestionItem(it) }
        "chat_prompt" -> message?.let { FeedItem.ChatPromptItem(it) }
        else -> null
    }
}
