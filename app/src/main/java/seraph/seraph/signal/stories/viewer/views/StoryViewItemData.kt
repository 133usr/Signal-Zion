package seraph.zion.signal.stories.viewer.views

import seraph.zion.signal.recipients.Recipient

data class StoryViewItemData(
  val recipient: Recipient,
  val timeViewedInMillis: Long
)
