package seraph.zion.signal.mms

import seraph.zion.signal.attachments.Attachment
import seraph.zion.signal.contactshare.Contact
import seraph.zion.signal.database.MessageType
import seraph.zion.signal.database.model.Mention
import seraph.zion.signal.database.model.ParentStoryId
import seraph.zion.signal.database.model.StoryType
import seraph.zion.signal.database.model.databaseprotos.BodyRangeList
import seraph.zion.signal.database.model.databaseprotos.DecryptedGroupV2Context
import seraph.zion.signal.database.model.databaseprotos.GiftBadge
import seraph.zion.signal.groups.GroupId
import seraph.zion.signal.linkpreview.LinkPreview
import seraph.zion.signal.recipients.RecipientId

class IncomingMessage(
  val type: MessageType,
  val from: RecipientId,
  val sentTimeMillis: Long,
  val serverTimeMillis: Long,
  val receivedTimeMillis: Long,
  val groupId: GroupId? = null,
  val groupContext: MessageGroupContext? = null,
  val body: String? = null,
  val storyType: StoryType = StoryType.NONE,
  val parentStoryId: ParentStoryId? = null,
  val isStoryReaction: Boolean = false,
  val subscriptionId: Int = -1,
  val expiresIn: Long = 0,
  val quote: QuoteModel? = null,
  val isUnidentified: Boolean = false,
  val isViewOnce: Boolean = false,
  val serverGuid: String? = null,
  val messageRanges: BodyRangeList? = null,
  attachments: List<Attachment> = emptyList(),
  sharedContacts: List<Contact> = emptyList(),
  linkPreviews: List<LinkPreview> = emptyList(),
  mentions: List<Mention> = emptyList(),
  val giftBadge: GiftBadge? = null
) {

  val attachments: List<Attachment> = ArrayList(attachments)
  val sharedContacts: List<Contact> = ArrayList(sharedContacts)
  val linkPreviews: List<LinkPreview> = ArrayList(linkPreviews)
  val mentions: List<Mention> = ArrayList(mentions)

  val isGroupMessage: Boolean = groupId != null

  companion object {
    @JvmStatic
    fun identityUpdate(from: RecipientId, sentTimestamp: Long, groupId: GroupId?): IncomingMessage {
      return IncomingMessage(
        from = from,
        sentTimeMillis = sentTimestamp,
        serverTimeMillis = -1,
        receivedTimeMillis = sentTimestamp,
        groupId = groupId,
        type = MessageType.IDENTITY_UPDATE
      )
    }

    @JvmStatic
    fun identityVerified(from: RecipientId, sentTimestamp: Long, groupId: GroupId?): IncomingMessage {
      return IncomingMessage(
        from = from,
        sentTimeMillis = sentTimestamp,
        serverTimeMillis = -1,
        receivedTimeMillis = sentTimestamp,
        groupId = groupId,
        type = MessageType.IDENTITY_VERIFIED
      )
    }

    @JvmStatic
    fun identityDefault(from: RecipientId, sentTimestamp: Long, groupId: GroupId?): IncomingMessage {
      return IncomingMessage(
        from = from,
        sentTimeMillis = sentTimestamp,
        serverTimeMillis = -1,
        receivedTimeMillis = sentTimestamp,
        groupId = groupId,
        type = MessageType.IDENTITY_DEFAULT
      )
    }

    fun contactJoined(from: RecipientId, currentTime: Long): IncomingMessage {
      return IncomingMessage(
        from = from,
        sentTimeMillis = currentTime,
        serverTimeMillis = -1,
        receivedTimeMillis = currentTime,
        type = MessageType.CONTACT_JOINED
      )
    }

    @JvmStatic
    fun groupUpdate(from: RecipientId, timestamp: Long, groupId: GroupId, groupContext: DecryptedGroupV2Context): IncomingMessage {
      val messageGroupContext = MessageGroupContext(groupContext)

      return IncomingMessage(
        from = from,
        sentTimeMillis = timestamp,
        receivedTimeMillis = timestamp,
        serverTimeMillis = timestamp,
        groupId = groupId,
        groupContext = messageGroupContext,
        body = messageGroupContext.encodedGroupContext,
        type = MessageType.GROUP_UPDATE
      )
    }
  }
}
