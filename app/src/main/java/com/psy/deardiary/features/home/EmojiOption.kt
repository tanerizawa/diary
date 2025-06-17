package com.psy.deardiary.features.home

data class EmojiOption(val emoji: String, val label: String)

const val NEUTRAL_EMOJI = "\uD83D\uDE10" // grey neutral face

val emojiOptions = listOf(
    EmojiOption("\uD83D\uDE00", "Sangat Senang"),
    EmojiOption("\uD83D\uDE42", "Senang"),
    EmojiOption(NEUTRAL_EMOJI, "Netral"),
    EmojiOption("\uD83D\uDE22", "Sedih"),
    EmojiOption("\uD83D\uDE21", "Marah")
)

