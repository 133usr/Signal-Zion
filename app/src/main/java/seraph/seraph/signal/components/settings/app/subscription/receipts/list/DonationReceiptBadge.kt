package seraph.zion.signal.components.settings.app.subscription.receipts.list

import seraph.zion.signal.badges.models.Badge
import seraph.zion.signal.database.model.DonationReceiptRecord

data class DonationReceiptBadge(
  val type: DonationReceiptRecord.Type,
  val level: Int,
  val badge: Badge
)
