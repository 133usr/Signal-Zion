package seraph.zion.signal.conversation.mutiselect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import seraph.zion.signal.attachments.Attachment
import seraph.zion.signal.conversation.ConversationMessage
import seraph.zion.signal.conversation.MessageSendType
import seraph.zion.signal.database.model.MessageRecord
import seraph.zion.signal.database.model.MmsMessageRecord
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.mms.MediaConstraints
import seraph.zion.signal.mms.SlideDeck
import seraph.zion.signal.mms.TextSlide
import seraph.zion.signal.util.Util

/**
 * General helper object for all things multiselect. This is only utilized by
 * [ConversationMessage]
 */
object Multiselect {

  /**
   * Returns a list of parts in the order in which they would appear to the user.
   */
  @JvmStatic
  fun getParts(conversationMessage: ConversationMessage): MultiselectCollection {
    val messageRecord = conversationMessage.messageRecord

    if (messageRecord.isUpdate) {
      return MultiselectCollection.Single(MultiselectPart.Update(conversationMessage))
    }

    val parts: LinkedHashSet<MultiselectPart> = linkedSetOf()

    if (messageRecord is MmsMessageRecord) {
      parts.addAll(getMmsParts(conversationMessage, messageRecord))
    }

    if (messageRecord.body.isNotEmpty()) {
      parts.add(MultiselectPart.Text(conversationMessage))
    }

    return if (parts.isEmpty()) {
      MultiselectCollection.Single(MultiselectPart.Message(conversationMessage))
    } else {
      MultiselectCollection.fromSet(parts)
    }
  }

  private fun getMmsParts(conversationMessage: ConversationMessage, mmsMessageRecord: MmsMessageRecord): Set<MultiselectPart> {
    val parts: LinkedHashSet<MultiselectPart> = linkedSetOf()

    val slideDeck: SlideDeck = mmsMessageRecord.slideDeck

    if (slideDeck.slides.filterNot { it is TextSlide }.isNotEmpty()) {
      parts.add(MultiselectPart.Attachments(conversationMessage))
    }

    if (slideDeck.body.isNotEmpty()) {
      parts.add(MultiselectPart.Text(conversationMessage))
    }

    return parts
  }

  fun canSendToNonPush(context: Context, multiselectPart: MultiselectPart): Boolean {
    return when (multiselectPart) {
      is MultiselectPart.Attachments -> canSendAllAttachmentsToNonPush(context, multiselectPart.conversationMessage.messageRecord)
      is MultiselectPart.Message -> canSendAllAttachmentsToNonPush(context, multiselectPart.conversationMessage.messageRecord)
      is MultiselectPart.Text -> true
      is MultiselectPart.Update -> throw AssertionError("Should never get to here.")
    }
  }

  /**
   * Helper function to determine whether a given attachment can be sent via MMS.
   */
  fun isMmsSupported(context: Context, mediaUri: Uri, mediaType: String, mediaSize: Long): Boolean {
    val canReadPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    if (!Util.isDefaultSmsProvider(context) || !canReadPhoneState || !Util.isMmsCapable(context) || !SignalStore.misc().smsExportPhase.allowSmsFeatures()) {
      return false
    }

    val sendType: MessageSendType = MessageSendType.getFirstForTransport(MessageSendType.TransportType.SMS)

    val mmsConstraints = MediaConstraints.getMmsMediaConstraints(sendType.simSubscriptionId ?: -1)
    return mmsConstraints.isSatisfied(context, mediaUri, mediaType, mediaSize) || mmsConstraints.canResize(mediaType)
  }

  private fun canSendAllAttachmentsToNonPush(context: Context, messageRecord: MessageRecord): Boolean {
    return if (messageRecord is MmsMessageRecord) {
      messageRecord.slideDeck.asAttachments().all { isMmsSupported(context, it) }
    } else {
      true
    }
  }

  /**
   * Helper function to determine whether a given attachment can be sent via MMS.
   */
  private fun isMmsSupported(context: Context, attachment: Attachment): Boolean {
    val canReadPhoneState = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    if (!Util.isDefaultSmsProvider(context) || !canReadPhoneState || !Util.isMmsCapable(context) || !SignalStore.misc().smsExportPhase.allowSmsFeatures()) {
      return false
    }

    val sendType: MessageSendType = MessageSendType.getFirstForTransport(MessageSendType.TransportType.SMS)

    val mmsConstraints = MediaConstraints.getMmsMediaConstraints(sendType.simSubscriptionId ?: -1)
    return mmsConstraints.isSatisfied(context, attachment) || mmsConstraints.canResize(attachment)
  }
}
