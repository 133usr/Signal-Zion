package seraph.zion.signal.database.model

import seraph.zion.signal.recipients.RecipientId

/** A model for [seraph.zion.signal.database.PendingRetryReceiptTable] */
data class PendingRetryReceiptModel(
  val id: Long,
  val author: RecipientId,
  val authorDevice: Int,
  val sentTimestamp: Long,
  val receivedTimestamp: Long,
  val threadId: Long
)
