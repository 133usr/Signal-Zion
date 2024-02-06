package seraph.zion.signal.safety

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import seraph.zion.signal.contacts.paged.ContactSearchKey
import seraph.zion.signal.database.model.MessageId
import seraph.zion.signal.recipients.RecipientId

/**
 * Fragment argument for `SafetyNumberBottomSheetFragment`
 */
@Parcelize
data class SafetyNumberBottomSheetArgs(
  val untrustedRecipients: List<RecipientId>,
  val destinations: List<ContactSearchKey.RecipientSearchKey>,
  val messageId: MessageId? = null
) : Parcelable
