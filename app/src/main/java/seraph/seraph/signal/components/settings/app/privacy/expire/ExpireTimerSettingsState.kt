package seraph.zion.signal.components.settings.app.privacy.expire

import seraph.zion.signal.util.livedata.ProcessState

data class ExpireTimerSettingsState(
  val initialTimer: Int = 0,
  val userSetTimer: Int? = null,
  val saveState: ProcessState<Int> = ProcessState.Idle(),
  val isGroupCreate: Boolean = false,
  val isForRecipient: Boolean = isGroupCreate
) {
  val currentTimer: Int
    get() = userSetTimer ?: initialTimer
}
