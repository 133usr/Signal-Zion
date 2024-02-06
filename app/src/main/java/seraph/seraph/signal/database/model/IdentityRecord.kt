package seraph.zion.signal.database.model

import org.signal.libsignal.protocol.IdentityKey
import seraph.zion.signal.database.IdentityTable
import seraph.zion.signal.recipients.RecipientId

data class IdentityRecord(
  val recipientId: RecipientId,
  val identityKey: IdentityKey,
  val verifiedStatus: IdentityTable.VerifiedStatus,
  @get:JvmName("isFirstUse")
  val firstUse: Boolean,
  val timestamp: Long,
  @get:JvmName("isApprovedNonBlocking")
  val nonblockingApproval: Boolean
)
