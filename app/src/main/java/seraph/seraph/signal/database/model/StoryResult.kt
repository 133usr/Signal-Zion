package seraph.zion.signal.database.model

import seraph.zion.signal.recipients.RecipientId

class StoryResult(
  val recipientId: RecipientId,
  val messageId: Long,
  val messageSentTimestamp: Long,
  val isOutgoing: Boolean
)
