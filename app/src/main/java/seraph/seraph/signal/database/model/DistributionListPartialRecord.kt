package seraph.zion.signal.database.model

import seraph.zion.signal.recipients.RecipientId

data class DistributionListPartialRecord(
  val id: DistributionListId,
  val name: CharSequence,
  val recipientId: RecipientId,
  val allowsReplies: Boolean,
  val isUnknown: Boolean,
  val privacyMode: DistributionListPrivacyMode
)
