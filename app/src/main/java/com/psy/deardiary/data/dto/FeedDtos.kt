package com.psy.deardiary.data.dto

import com.google.gson.annotations.SerializedName

data class ArticleResponse(
    val title: String,
    val source: String,
    @SerializedName("image_url") val imageUrl: String
)

data class FeedItemResponse(
    val type: String,
    val journal: JournalResponse?,
    val article: ArticleResponse?,
    val message: String?
)
