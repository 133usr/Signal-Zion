package seraph.zion.signal.safety

import seraph.zion.signal.database.model.DistributionListId
import seraph.zion.signal.recipients.Recipient

sealed class SafetyNumberBucket {
  data class DistributionListBucket(val distributionListId: DistributionListId, val name: String) : SafetyNumberBucket()
  data class GroupBucket(val recipient: Recipient) : SafetyNumberBucket()
  object ContactsBucket : SafetyNumberBucket()
}
