package seraph.zion.signal.stories.my

import seraph.zion.signal.conversation.ConversationMessage
import seraph.zion.signal.database.model.MessageRecord

data class MyStoriesState(
  val distributionSets: List<DistributionSet> = emptyList()
) {

  data class DistributionSet(
    val label: String?,
    val stories: List<DistributionStory>
  )

  data class DistributionStory(
    val message: ConversationMessage,
    val views: Int
  ) {
    val messageRecord: MessageRecord = message.messageRecord
  }
}
