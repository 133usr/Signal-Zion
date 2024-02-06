package seraph.zion.signal.conversationlist.chatfilter

import seraph.zion.signal.conversationlist.model.ConversationFilter

data class ConversationFilterRequest(
  val filter: ConversationFilter,
  val source: ConversationFilterSource
)
