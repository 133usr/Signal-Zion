package seraph.zion.signal.stories.viewer.reply.direct

import seraph.zion.signal.database.model.MessageRecord
import seraph.zion.signal.recipients.Recipient

data class StoryDirectReplyState(
  val groupDirectReplyRecipient: Recipient? = null,
  val storyRecord: MessageRecord? = null
)
