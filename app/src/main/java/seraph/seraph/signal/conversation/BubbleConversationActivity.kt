package seraph.zion.signal.conversation

import seraph.zion.signal.R
import seraph.zion.signal.conversation.v2.ConversationActivity
import seraph.zion.signal.util.ViewUtil

/**
 * Activity which encapsulates a conversation for a Bubble window.
 *
 * This activity exists so that we can override some of its manifest parameters
 * without clashing with [ConversationActivity] and provide an API-level
 * independent "is in bubble?" check.
 */
class BubbleConversationActivity : ConversationActivity() {
  override fun onPause() {
    super.onPause()
    ViewUtil.hideKeyboard(this, findViewById(R.id.fragment_container))
  }
}
