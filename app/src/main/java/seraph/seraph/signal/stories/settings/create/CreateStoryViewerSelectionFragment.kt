package seraph.zion.signal.stories.settings.create

import androidx.navigation.fragment.findNavController
import seraph.zion.signal.R
import seraph.zion.signal.database.model.DistributionListId
import seraph.zion.signal.recipients.RecipientId
import seraph.zion.signal.stories.settings.select.BaseStoryRecipientSelectionFragment
import seraph.zion.signal.util.navigation.safeNavigate

/**
 * Allows user to select who will see the story they are creating
 */
class CreateStoryViewerSelectionFragment : BaseStoryRecipientSelectionFragment() {
  override val actionButtonLabel: Int = R.string.CreateStoryViewerSelectionFragment__next
  override val distributionListId: DistributionListId? = null

  override fun goToNextScreen(recipients: Set<RecipientId>) {
    findNavController().safeNavigate(CreateStoryViewerSelectionFragmentDirections.actionCreateStoryViewerSelectionToCreateStoryWithViewers(recipients.toTypedArray()))
  }
}
