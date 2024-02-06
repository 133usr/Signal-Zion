package seraph.zion.signal.stories.settings.group

import seraph.zion.signal.recipients.Recipient

data class GroupStorySettingsState(
  val name: String = "",
  val members: List<Recipient> = emptyList(),
  val removed: Boolean = false
)
