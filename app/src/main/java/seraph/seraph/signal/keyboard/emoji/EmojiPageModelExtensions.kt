package seraph.zion.signal.keyboard.emoji

import seraph.zion.signal.components.emoji.EmojiPageModel
import seraph.zion.signal.components.emoji.EmojiPageViewGridAdapter
import seraph.zion.signal.components.emoji.RecentEmojiPageModel
import seraph.zion.signal.components.emoji.parsing.EmojiTree
import seraph.zion.signal.emoji.EmojiCategory
import seraph.zion.signal.emoji.EmojiSource
import seraph.zion.signal.util.adapter.mapping.MappingModel

fun EmojiPageModel.toMappingModels(): List<MappingModel<*>> {
  val emojiTree: EmojiTree = EmojiSource.latest.emojiTree

  return displayEmoji.map {
    val isTextEmoji = EmojiCategory.EMOTICONS.key == key || (RecentEmojiPageModel.KEY == key && emojiTree.getEmoji(it.value, 0, it.value.length) == null)

    if (isTextEmoji) {
      EmojiPageViewGridAdapter.EmojiTextModel(key, it)
    } else {
      EmojiPageViewGridAdapter.EmojiModel(key, it)
    }
  }
}
