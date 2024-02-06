package seraph.zion.signal.components.emoji.parsing

import seraph.zion.signal.emoji.EmojiPage

data class EmojiDrawInfo(val page: EmojiPage, val index: Int, private val emoji: String, val rawEmoji: String?, val jumboSheet: String?)
