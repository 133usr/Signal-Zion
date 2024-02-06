package seraph.zion.signal.components.settings.conversation.sounds.custom

import android.net.Uri
import seraph.zion.signal.database.RecipientTable
import seraph.zion.signal.recipients.Recipient

data class CustomNotificationsSettingsState(
  val isInitialLoadComplete: Boolean = false,
  val recipient: Recipient? = null,
  val hasCustomNotifications: Boolean = false,
  val controlsEnabled: Boolean = false,
  val messageVibrateState: RecipientTable.VibrateState = RecipientTable.VibrateState.DEFAULT,
  val messageVibrateEnabled: Boolean = false,
  val messageSound: Uri? = null,
  val callVibrateState: RecipientTable.VibrateState = RecipientTable.VibrateState.DEFAULT,
  val callSound: Uri? = null,
  val showCallingOptions: Boolean = false
)
