package seraph.zion.signal.components.settings.conversation

import seraph.zion.signal.util.DynamicNoActionBarTheme
import seraph.zion.signal.util.DynamicTheme

class CallInfoActivity : ConversationSettingsActivity(), ConversationSettingsFragment.Callback {

  override val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()
}
