package seraph.zion.signal.conversation.ui.inlinequery

import seraph.zion.signal.R
import seraph.zion.signal.util.adapter.mapping.AnyMappingModel
import seraph.zion.signal.util.adapter.mapping.MappingAdapter

class InlineQueryAdapter(listener: (AnyMappingModel) -> Unit) : MappingAdapter() {
  init {
    registerFactory(InlineQueryEmojiResult.Model::class.java, { InlineQueryEmojiResult.ViewHolder(it, listener) }, R.layout.inline_query_emoji_result)
  }
}
