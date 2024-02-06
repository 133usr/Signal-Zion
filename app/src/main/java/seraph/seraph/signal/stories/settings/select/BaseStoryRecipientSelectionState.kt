package seraph.zion.signal.stories.settings.select

import seraph.zion.signal.database.model.DistributionListId
import seraph.zion.signal.database.model.DistributionListRecord
import seraph.zion.signal.recipients.RecipientId

data class BaseStoryRecipientSelectionState(
  val distributionListId: DistributionListId?,
  val privateStory: DistributionListRecord? = null,
  val selection: Set<RecipientId> = emptySet()
)
