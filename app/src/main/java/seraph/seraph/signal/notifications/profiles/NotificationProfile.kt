package seraph.zion.signal.notifications.profiles

import seraph.zion.signal.conversation.colors.AvatarColor
import seraph.zion.signal.recipients.RecipientId

data class NotificationProfile(
  val id: Long,
  val name: String,
  val emoji: String,
  val color: AvatarColor = AvatarColor.A210,
  val createdAt: Long,
  val allowAllCalls: Boolean = true,
  val allowAllMentions: Boolean = false,
  val schedule: NotificationProfileSchedule,
  val allowedMembers: Set<RecipientId> = emptySet()
) : Comparable<NotificationProfile> {

  fun isRecipientAllowed(id: RecipientId): Boolean {
    return allowedMembers.contains(id)
  }

  override fun compareTo(other: NotificationProfile): Int {
    return createdAt.compareTo(other.createdAt)
  }
}
