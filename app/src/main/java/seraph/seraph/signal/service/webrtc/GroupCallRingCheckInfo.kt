package seraph.zion.signal.service.webrtc

import org.signal.ringrtc.CallManager
import seraph.zion.signal.groups.GroupId
import seraph.zion.signal.recipients.RecipientId
import org.whispersystems.signalservice.api.push.ServiceId.ACI

data class GroupCallRingCheckInfo(
  val recipientId: RecipientId,
  val groupId: GroupId.V2,
  val ringId: Long,
  val ringerAci: ACI,
  val ringUpdate: CallManager.RingUpdate
)
