package seraph.zion.signal.components.settings.app.notifications.manual

import seraph.zion.signal.notifications.profiles.NotificationProfile
import java.time.LocalDateTime

data class NotificationProfileSelectionState(
  val notificationProfiles: List<NotificationProfile> = listOf(),
  val expandedId: Long = -1L,
  val timeSlotB: LocalDateTime
)
