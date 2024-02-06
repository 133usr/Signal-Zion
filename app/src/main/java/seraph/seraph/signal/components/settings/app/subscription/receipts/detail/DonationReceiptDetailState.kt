package seraph.zion.signal.components.settings.app.subscription.receipts.detail

import seraph.zion.signal.database.model.DonationReceiptRecord

data class DonationReceiptDetailState(
  val donationReceiptRecord: DonationReceiptRecord? = null,
  val subscriptionName: String? = null
)
