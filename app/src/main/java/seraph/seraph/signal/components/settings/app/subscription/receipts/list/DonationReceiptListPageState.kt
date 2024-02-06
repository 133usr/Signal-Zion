package seraph.zion.signal.components.settings.app.subscription.receipts.list

import seraph.zion.signal.database.model.DonationReceiptRecord

data class DonationReceiptListPageState(
  val records: List<DonationReceiptRecord> = emptyList(),
  val isLoaded: Boolean = false
)
