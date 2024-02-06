package seraph.zion.signal.badges.gifts.viewgift.sent

import seraph.zion.signal.badges.models.Badge
import seraph.zion.signal.recipients.Recipient

data class ViewSentGiftState(
  val recipient: Recipient? = null,
  val badge: Badge? = null
)
